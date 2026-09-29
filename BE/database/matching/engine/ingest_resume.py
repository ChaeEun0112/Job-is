"""이력서 인제스트 (구현 순서 3단계): 파싱 · 임베딩 · 스킬추출 → 신호②(역량/증거).

정밀도 최대 레버. 이력서를 공고와 '같은 축'에 올린다:
  · 임베딩(384d) → user_documents.embedding : JD 와 대칭 의미매칭
  · 스킬추출 → user_skills : **공고 스킬 어휘(canonical_id) 재사용** → 공고 skill_tag_ids 와 동일 좌표
  · 경력/도메인 파싱 → parsed jsonb + LLM 근거 컨텍스트 재료

어휘 사전은 DB 의 권위 태그(skills_inferred=false)에서 직접 구축한다(코퍼스=사전).

  export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis
  python ingest_resume.py --persona p2-senior-backend
"""
from __future__ import annotations
import argparse, json, os, re, sys
import numpy as np
import psycopg2
from psycopg2.extras import Json, RealDictCursor
from fastembed import TextEmbedding
from retrieve import MODEL

ALIASES = {"k8s": "kubernetes", "msa": "msa", "마이크로서비스": "msa",
           "es": "elasticsearch", "rest api": "rest api"}
# 너무 일반적이라 매칭 신호가 되지 못하는 토큰(공고쪽 skill_enrich 와 동일 정책)
DENY = {"개발", "서버", "보안", "인프라", "디자인", "기획", "api", "rest", "apache",
        "백엔드 개발", "프론트엔드 개발", "웹 개발", "앱 개발", "서비스 기획"}
ASCII_TOKEN = re.compile(r"^[a-z0-9 .+#/-]+$")


def load_vocab(cur):
    """공고 API 태그에서 skill명(lower) → canonical_id 사전."""
    cur.execute("""
                WITH v AS (
                    SELECT lower(t.skill) AS skill, t.id AS canonical_id
                    FROM job_postings jp, unnest(jp.skill_tags, jp.skill_tag_ids) AS t(skill, id)
                    WHERE jp.skills_inferred = false AND jp.skill_tag_ids <> '{}')
                SELECT skill, mode() WITHIN GROUP (ORDER BY canonical_id) AS canonical_id
                FROM v GROUP BY skill""")
    return {r["skill"]: r["canonical_id"] for r in cur.fetchall()}


def extract_skills(text, vocab):
    """이력서 본문에서 어휘 매칭 → [(skill_display, canonical_id, evidence_line)]."""
    lines = [l.strip() for l in text.splitlines() if l.strip()]
    low = text.lower()
    found = {}
    for skill, cid in vocab.items():
        if len(skill) < 2 or skill in DENY:
            continue
        if ASCII_TOKEN.match(skill):
            if not re.search(r"(?<![a-z0-9])" + re.escape(skill) + r"(?![a-z0-9])", low):
                continue
        elif skill not in low:
            continue
        ev = next((l for l in lines if skill in l.lower()), None)
        found[skill] = (cid, ev)
    return found


def parse_meta(text):
    m = re.search(r"경력\s*(\d+)\s*년", text) or re.search(r"(\d+)\s*년간", text)
    years = int(m.group(1)) if m else None
    title = next((l.strip() for l in text.splitlines() if l.strip()), "")
    return {"career_years": years, "title_line": title}


def vec_literal(row) -> str:
    return "[" + ",".join(f"{x:.6f}" for x in row) + "]"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument("--personas", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"))
    ap.add_argument("--file", default=None, help="기본 resumes/<persona>.txt")
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    a = ap.parse_args()
    if not a.dsn:
        sys.exit("DATABASE_URL(또는 --dsn) 필요")

    personas = {p["id"]: p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas[a.persona]
    path = a.file or os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "resumes", f"{a.persona}.txt")
    text = open(path, encoding="utf-8").read()

    conn = psycopg2.connect(a.dsn)
    conn.autocommit = False
    cur = conn.cursor(cursor_factory=RealDictCursor)

    # 유저 확보(신호① 없이 이력서만 넣는 경우도 지원)
    cur.execute("INSERT INTO users (ext_ref) VALUES (%s) "
                "ON CONFLICT (ext_ref) DO UPDATE SET ext_ref=EXCLUDED.ext_ref RETURNING id",
                (persona["id"],))
    uid = cur.fetchone()["id"]

    vocab = load_vocab(cur)
    # 별칭 반영(어휘에 있으면 canonical 승계)
    for alias, canon in ALIASES.items():
        if canon in vocab and alias not in vocab:
            vocab[alias] = vocab[canon]
    skills = extract_skills(text, vocab)
    meta = parse_meta(text)

    # 임베딩
    model = TextEmbedding(model_name=MODEL)
    emb = np.array(list(model.embed([text[:2000]])), dtype=np.float32)[0]
    emb /= (np.linalg.norm(emb) + 1e-9)

    # user_documents (멱등: 기존 이력서 교체)
    cur.execute("DELETE FROM user_documents WHERE user_id=%s AND doc_type='resume'", (uid,))
    cur.execute(
        """INSERT INTO user_documents (user_id, doc_type, raw_text, parsed, embedding, is_active)
           VALUES (%s,'resume',%s,%s,%s::vector,true) RETURNING id""",
        (uid, text, Json({**meta, "skills": sorted(skills.keys())}), vec_literal(emb)))
    doc_id = cur.fetchone()["id"]

    # user_skills (멱등: source='resume' 교체)
    cur.execute("DELETE FROM user_skills WHERE user_id=%s AND source='resume'", (uid,))
    n_canon = 0
    for skill, (cid, ev) in sorted(skills.items()):
        if cid is not None:
            n_canon += 1
        cur.execute(
            """INSERT INTO user_skills (user_id, skill, canonical_id, source, weight, evidence)
               VALUES (%s,%s,%s,'resume',1.5,%s)
                   ON CONFLICT (user_id, skill, source) DO UPDATE SET
                canonical_id=EXCLUDED.canonical_id, weight=EXCLUDED.weight, evidence=EXCLUDED.evidence""",
            (uid, skill, cid, (ev or "")[:200]))

    # 확정된 연차를 선언 선호에도 반영(있으면 UPSERT)
    if meta["career_years"] is not None:
        cur.execute(
            """
            INSERT INTO user_preferences (user_id, career_years, updated_at)
            VALUES (%s, %s, now())
                ON CONFLICT (user_id) DO UPDATE
                                             SET career_years = EXCLUDED.career_years, updated_at = now()
            """,
            (uid, meta["career_years"])
        )
    conn.commit()

    print(f"[resume] user#{uid} doc#{doc_id}: 스킬 {len(skills)}개 추출 "
          f"(canonical 매핑 {n_canon}개), 연차 {meta['career_years']}")
    print("  스킬:", ", ".join(f"{s}#{c}" if c else s for s, (c, _) in sorted(skills.items())))
    cur.close(); conn.close()


if __name__ == "__main__":
    main()
