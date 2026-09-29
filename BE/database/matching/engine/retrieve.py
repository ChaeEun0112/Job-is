"""하이브리드 후보 검색: 유저 프로필 → (의미 임베딩 유사도 + 정형 필터/부스트) → 후보군.

- 의미(recall): 프로필 텍스트 임베딩 vs 공고 임베딩 코사인
- 정형 하드필터: 지역(도시 or 원격), 경력(공고 최소경력 ≤ 내 경력), 제외 키워드
- 정형 소프트부스트: 스킬 교집합, 선호 기업규모
→ LLM 최종 선별(select) 로 넘길 후보 top-N.

사용:
  python retrieve.py --persona p2-senior-backend --topn 20
"""
from __future__ import annotations
import argparse, json, os
import numpy as np
from fastembed import TextEmbedding
from constants import SIZE_ALIAS

MODEL = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"


def load(data_dir):
    vecs = np.load(os.path.join(data_dir, "embeddings.npy"))
    meta = [json.loads(l) for l in open(os.path.join(data_dir, "meta.jsonl"), encoding="utf-8")]
    return vecs, meta


def profile_query(p):
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


def retrieve(persona, vecs, meta, model, topn=20):
    q = model.query_embed(profile_query(persona)) if hasattr(model, "query_embed") else model.embed([profile_query(persona)])
    qv = np.array(list(q), dtype=np.float32)[0]
    qv /= (np.linalg.norm(qv) + 1e-9)
    cos = vecs @ qv                                   # 정규화돼 있어 내적=코사인

    locs = set(persona.get("locations") or [])
    yrs = persona.get("career_years")
    excl = [e.lower() for e in (persona.get("exclude") or [])]
    pskills = {s.lower() for s in (persona.get("skills") or [])}
    prefs = persona.get("company_size_pref") or []

    out = []
    for i, m in enumerate(meta):
        # 하드필터
        if locs:
            if not ((m.get("location_city") in locs) or (persona.get("remote_ok") and m.get("is_remote"))):
                continue
        if yrs is not None and m.get("career_min") is not None and m["career_min"] > yrs:
            continue
        blob = f"{m.get('position','')} {m.get('company','')} {m.get('req_snippet','')} {m.get('tasks_snippet','')}".lower()
        if excl and any(e in blob for e in excl):
            continue
        # 소프트부스트
        overlap = len(pskills & {s.lower() for s in (m.get("skill_tags") or [])})
        overlap_n = overlap / max(1, len(pskills))
        size_b = 1.0 if size_ok(m.get("company_type"), prefs) else 0.0
        final = float(cos[i]) + 0.15 * overlap_n + 0.05 * size_b
        out.append({**m, "score_cosine": round(float(cos[i]), 4),
                    "skill_overlap": overlap, "score_final": round(final, 4)})
    out.sort(key=lambda x: x["score_final"], reverse=True)
    return out[:topn]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument("--personas", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"))
    ap.add_argument("--data", default=".")
    ap.add_argument("--topn", type=int, default=20)
    ap.add_argument("--out", default=None)
    a = ap.parse_args()
    personas = {p["id"]: p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas[a.persona]
    vecs, meta = load(a.data)
    model = TextEmbedding(model_name=MODEL)
    cands = retrieve(persona, vecs, meta, model, a.topn)
    print(f"[{persona['label']}] 후보 {len(cands)}건 (topn={a.topn})")
    for c in cands[:a.topn]:
        print(f"  {c['score_final']:.3f} (cos {c['score_cosine']:.3f}, skill×{c['skill_overlap']}) "
              f"{c['position'][:34]} @ {c['company']} [{c.get('company_type')}/{c.get('employee_count')}명]")
    out = a.out or f"candidates_{a.persona}.json"
    json.dump({"persona": persona, "candidates": cands}, open(out, "w"), ensure_ascii=False, indent=2)
    print(f"→ {out}")


if __name__ == "__main__":
    main()
