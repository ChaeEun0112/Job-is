"""페르소나 → 벡터 유사도 기반 공고 추천 (stdout = JSON only).

Java(JobSimilarService)가 이 스크립트를 실행하고 stdout 을 그대로 JSON 파싱한다.
따라서 진단 로그는 전부 stderr 로만 출력한다.

동작:
  1) personas.json 에서 페르소나 로드 (id 대소문자 무시)
  2) DATABASE_URL 로 접속, job_postings + companies 조회 (컬럼은 실제 스키마를 introspect)
  3) 공고 텍스트 임베딩 → 유저 쿼리 임베딩과 코사인 유사도 계산
     · fastembed 사용 가능하면 ONNX 모델 사용
     · 불가하면 해시 기반 384차원 TF 벡터로 폴백 (의존성 없음)
  4) 코사인 + 스킬 교집합 + 기업규모 선호 보정 → score_final 내림차순
  5) job_postings.embedding(TEXT) 에 계산된 벡터를 캐싱 (다음 실행 재사용)

  set DATABASE_URL=postgresql://postgres:1234@localhost:5432/jobisbe
  python retrieve_user_json.py --persona sb --topn 20
"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import sys
import urllib.request

import numpy as np
import psycopg2
from psycopg2.extras import RealDictCursor
from constants import SIZE_ALIAS

DIM = 384


def log(msg):
    print(msg, file=sys.stderr)


# ---------------------------------------------------------------- 임베딩
class HashingEmbedder:
    """의존성 없는 폴백 임베더. 토큰 해시 → 384차원 TF 벡터 (L2 정규화)."""

    name = "hashing-384"

    @staticmethod
    def _tokens(text):
        text = (text or "").lower()
        words = re.findall(r"[a-z0-9]+|[가-힣]+", text)
        out = []
        for w in words:
            out.append(w)
            # 한글은 bigram 으로 부분 일치 확보
            if len(w) > 1 and not w.isascii():
                out.extend(w[i:i + 2] for i in range(len(w) - 1))
        return out

    def embed(self, texts):
        mat = np.zeros((len(texts), DIM), dtype=np.float32)
        for i, t in enumerate(texts):
            for tok in self._tokens(t):
                h = int(hashlib.md5(tok.encode("utf-8")).hexdigest()[:8], 16)
                mat[i, h % DIM] += 1.0
        mat /= (np.linalg.norm(mat, axis=1, keepdims=True) + 1e-9)
        return mat


class FastEmbedEmbedder:
    name = "fastembed-MiniLM-L12-v2"
    MODEL = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"

    def __init__(self):
        from fastembed import TextEmbedding  # noqa: WPS433
        self.model = TextEmbedding(model_name=self.MODEL)

    def embed(self, texts):
        mat = np.array(list(self.model.embed(texts)), dtype=np.float32)
        mat /= (np.linalg.norm(mat, axis=1, keepdims=True) + 1e-9)
        return mat


def build_embedder():
    if os.environ.get("EMBEDDER", "").lower() == "hashing":
        log("[embed] EMBEDDER=hashing 지정 → 해시 임베더 사용")
        return HashingEmbedder()
    try:
        emb = FastEmbedEmbedder()
        log("[embed] fastembed 사용")
        return emb
    except Exception as e:  # 미설치/모델 다운로드 실패 등
        log(f"[embed] fastembed 사용 불가({type(e).__name__}: {e}) → 해시 임베더로 폴백")
        return HashingEmbedder()


# ---------------------------------------------------------------- DB
def existing_columns(cur, table):
    cur.execute(
        "SELECT column_name FROM information_schema.columns WHERE table_name=%s",
        (table,),
    )
    return {r["column_name"] for r in cur.fetchall()}


def column_udt(cur, table, column):
    """컬럼의 실제 타입명(udt_name). 없으면 None."""
    cur.execute(
        "SELECT udt_name FROM information_schema.columns "
        "WHERE table_name=%s AND column_name=%s",
        (table, column),
    )
    row = cur.fetchone()
    return (row or {}).get("udt_name")


def fetch_jobs(cur, limit):
    cols = existing_columns(cur, "job_postings")
    ccols = existing_columns(cur, "companies")

    def jp(name, alias=None):
        return f"jp.{name} AS {alias or name}" if name in cols else f"NULL AS {alias or name}"

    def co(name):
        return f"c.{name} AS {name}" if name in ccols else f"NULL AS {name}"

    title_expr = (
        "COALESCE(jp.position, jp.title)" if {"position", "title"} <= cols
        else ("jp.position" if "position" in cols else "jp.title")
    )

    select = ", ".join([
        "jp.id AS id",
        jp("external_id"),
        f"{title_expr} AS position",
        co("name"),
        co("company_type"),
        co("employee_count"),
        jp("location_city"),
        jp("location_full"),
        jp("is_remote"),
        jp("career_min"),
        jp("career_max"),
        jp("skill_tags"),
        jp("source_url"),
        jp("main_tasks"),
        jp("requirements"),
        jp("preferred_points"),
        jp("intro"),
        jp("embedding"),
    ])

    where = "WHERE COALESCE(jp.status,'active') NOT IN ('REMOVED','removed')" if "status" in cols else ""
    sql = (
        f"SELECT {select} FROM job_postings jp "
        f"LEFT JOIN companies c ON c.id = jp.company_id {where} "
        f"ORDER BY jp.id DESC LIMIT %(limit)s"
    )
    cur.execute(sql, {"limit": limit})
    return cur.fetchall(), cols


def job_text(r):
    parts = [
        r.get("position"), r.get("name"),
        " ".join(r.get("skill_tags") or []),
        r.get("main_tasks"), r.get("requirements"),
        r.get("preferred_points"), r.get("intro"),
        r.get("location_full"),
    ]
    return " / ".join(p for p in parts if p)[:4000]


def persona_text(p):
    return " / ".join(filter(None, [
        " ".join(p.get("interest_roles") or []),
        "기술스택: " + ", ".join(p.get("skills") or []),
        p.get("free_text") or "",
    ]))


def size_ok(company_type, prefs):
    if not prefs or not company_type:
        return False
    allowed = set().union(*(SIZE_ALIAS.get(x, {x}) for x in prefs))
    return company_type in allowed


def parse_vec(txt):
    if not txt:
        return None
    try:
        v = np.array(json.loads(txt.replace("{", "[").replace("}", "]")), dtype=np.float32)
        return v if v.shape == (DIM,) else None
    except Exception:
        return None


def vec_literal(v):
    return "[" + ",".join(f"{x:.6f}" for x in v) + "]"


# ---------------------------------------------------------------- LLM 선별 (OpenAI)
_SYS = (
    "너는 Job.is의 취업 큐레이터다. 사용자 프로필과, 임베딩+정형필터로 1차 선별된 "
    "채용공고 후보군을 받는다. 후보 전체를 빠짐없이 포함해야 하며, 각 공고에 대해 "
    "프로필↔공고 근거(직무·기술·경력·기업규모·지역)를 2문장으로 구체적으로 설명한다. "
    "첫 문장은 핵심 적합 이유, 두 번째 문장은 이 공고에서 특히 주목할 점을 쓴다. "
    "공고를 임의로 제외하지 말 것. 과장 금지, 근거 중심. "
    "반드시 아래 JSON 스키마로만 답한다(설명 텍스트 없이 JSON만).\n"
    '{"recommendations":[{"rank":1,"external_id":0,"reason":"두 문장 핵심 추천 이유",'
    '"fit_points":["근거1","근거2"],"caution":"주의점(없으면 빈 문자열)"}]}'
)

_OPENAI_API = "https://api.openai.com/v1/chat/completions"


def _select_openai(persona, candidates, api_key, model):
    cand_slim = [{
        "external_id": c["external_id"], "position": c["position"], "company": c["company"],
        "location": c.get("location_full"), "skill_tags": c.get("skill_tags"),
        "score_cosine": c.get("score_cosine"),
    } for c in candidates]
    user_prompt = (
        f"[사용자 프로필]\n{json.dumps(persona, ensure_ascii=False)}\n\n"
        f"[후보 공고 {len(cand_slim)}건]\n{json.dumps(cand_slim, ensure_ascii=False)}\n\n"
        f"위 후보 {len(cand_slim)}건 전부를 빠짐없이 포함해 스키마 JSON으로만 답하라. "
        f"recommendations 배열의 길이는 반드시 {len(cand_slim)}이어야 한다."
    )
    body = json.dumps({
        "model": model,
        "max_tokens": 4000,
        "messages": [
            {"role": "system", "content": _SYS},
            {"role": "user", "content": user_prompt},
        ],
    }).encode()
    req = urllib.request.Request(_OPENAI_API, data=body, headers={
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
    })
    with urllib.request.urlopen(req, timeout=90) as r:
        resp = json.loads(r.read())
    text = resp["choices"][0]["message"]["content"].strip()
    text = text.removeprefix("```json").removeprefix("```").removesuffix("```").strip()
    return json.loads(text)


# ---------------------------------------------------------------- main
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument(
        "--personas",
        default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"),
    )
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    ap.add_argument("--topn", type=int, default=20)
    ap.add_argument("--overfetch", type=int, default=300)
    a = ap.parse_args()

    if not a.dsn:
        log("DATABASE_URL(또는 --dsn) 필요")
        print(json.dumps({"error": "no dsn", "candidates": []}, ensure_ascii=False))
        return 1

    personas = {p["id"].lower(): p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas.get(a.persona.lower())
    if persona is None:
        log(f"페르소나 없음: {a.persona} (가능: {sorted(personas)})")
        print(json.dumps({"error": f"unknown persona {a.persona}", "candidates": []}, ensure_ascii=False))
        return 1

    conn = psycopg2.connect(a.dsn)
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        rows, cols = fetch_jobs(cur, a.overfetch)
    log(f"[db] 후보 원본 {len(rows)}건")

    if not rows:
        conn.close()
        print(json.dumps({"persona": persona, "candidates": []}, ensure_ascii=False))
        return 0

    embedder = build_embedder()

    # 캐시된 임베딩 재사용, 없는 것만 계산
    cached = {i: parse_vec(r.get("embedding")) for i, r in enumerate(rows)}
    todo = [i for i, v in cached.items() if v is None]
    if todo:
        log(f"[embed] 신규 계산 {len(todo)}건 / 캐시 {len(rows) - len(todo)}건")
        vecs_new = embedder.embed([job_text(rows[i]) for i in todo])
        for k, i in enumerate(todo):
            cached[i] = vecs_new[k]

    mat = np.vstack([cached[i] for i in range(len(rows))])
    qv = embedder.embed([persona_text(persona)])[0]
    cos = mat @ qv  # 정규화된 벡터의 내적 = 코사인 유사도

    # 임베딩 캐시 저장 (embedding 컬럼이 있을 때만)
    if todo and "embedding" in cols:
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as tcur:
                emb_udt = column_udt(tcur, "job_postings", "embedding")
            # user_schema.sql 은 embedding 을 vector(384) 로 만든다. 이때 캐스트가 없으면
            # 텍스트→vector 타입 불일치로 UPDATE 가 실패한다(아래 except 가 삼켜 캐시가 영구 미적재).
            # TEXT 로 만들어진 스키마도 있을 수 있어 실제 타입을 보고 캐스트를 붙인다.
            cast = "::vector" if emb_udt == "vector" else ""
            with conn.cursor() as wcur:
                for i in todo:
                    # 읽을 때는 parse_vec() 이 "[...]" 문자열을 그대로 파싱한다(두 타입 모두 동일).
                    wcur.execute(
                        f"UPDATE job_postings SET embedding=%s{cast} WHERE id=%s",
                        (vec_literal(cached[i]), rows[i]["id"]),
                    )
            conn.commit()
            log(f"[db] 임베딩 캐시 저장 {len(todo)}건 (embedding={emb_udt or '미상'})")
        except Exception as e:
            conn.rollback()
            log(f"[db] 경고: 임베딩 캐시 저장 실패 — 추천은 계속하되 다음 실행도 재계산한다: {e}")
    conn.close()

    pskills = {s.lower() for s in (persona.get("skills") or [])}
    prefs = persona.get("company_size_pref") or []
    locs = set(persona.get("locations") or [])
    yrs = persona.get("career_years")
    excl = [e.lower() for e in (persona.get("exclude") or persona.get("excludes") or [])]
    remote_ok = bool(persona.get("remote_ok"))

    out = []
    for i, r in enumerate(rows):
        # 하드필터 (정보가 없으면 통과 — 데이터 결손으로 0건 되는 것 방지)
        if locs and r.get("location_city"):
            if r["location_city"] not in locs and not (remote_ok and r.get("is_remote")):
                continue
        if yrs is not None and r.get("career_min") is not None and r["career_min"] > yrs:
            continue
        blob = job_text(r).lower()
        if excl and any(e in blob for e in excl):
            continue

        tags = {t.lower() for t in (r.get("skill_tags") or [])}
        overlap = len(tags & pskills)
        c = float(cos[i])
        final = c + 0.15 * (overlap / max(1, len(pskills))) + 0.05 * (1.0 if size_ok(r.get("company_type"), prefs) else 0.0)
        out.append({
            "external_id": str(r.get("external_id") or r["id"]),
            "position": r.get("position") or "",
            "company": r.get("name") or "",
            "company_type": r.get("company_type"),
            "employee_count": r.get("employee_count"),
            "location_full": r.get("location_full"),
            "source_url": r.get("source_url"),
            "skill_tags": r.get("skill_tags") or [],
            "score_cosine": round(c, 4),
            "skill_overlap": overlap,
            "score_final": round(final, 4),
        })

    out.sort(key=lambda x: x["score_final"], reverse=True)
    top = out[:a.topn]
    log(f"[result] 필터 통과 {len(out)}건 → 상위 {len(top)}건 반환 (embedder={embedder.name})")

    # LLM 추천 이유 생성: OPENAI_API_KEY 있을 때 reason + fit_points 생성
    api_key = os.environ.get("OPENAI_API_KEY")
    if api_key:
        try:
            result = _select_openai(persona, top, api_key, os.environ.get("OPENAI_MODEL", "gpt-4o-mini"))
            llm_map = {str(r["external_id"]): r for r in result.get("recommendations", [])}
            for c in top:
                llm = llm_map.get(str(c["external_id"]))
                if llm:
                    c["reason"] = llm.get("reason") or ""
                    c["fit_points"] = llm.get("fit_points") or []
                    c["caution"] = llm.get("caution") or ""
            log(f"[llm] 추천 이유 생성 완료 ({len(llm_map)}건)")
        except Exception as e:
            log(f"[llm] LLM 추천 이유 생성 실패 — 임베딩 결과로 대체: {e}")

    print(json.dumps(
        {"persona": persona, "embedder": embedder.name, "candidates": top},
        ensure_ascii=False,
    ))
    return 0


if __name__ == "__main__":
    sys.exit(main())
