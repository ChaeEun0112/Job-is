"""엔드투엔드 (DB 유저신호 통합): 신호①②(④) → 후보검색 → LLM 근거기반 선별 → top-K.

recommend_pg 의 상위 버전. 후보검색을 retrieve_user(이력서 블렌드+동일축 스킬)로, LLM 선별에
이력서 근거(evidence)를 주입해 '근거 있는 추천 이유'를 생성한다.

  export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis
  export ANTHROPIC_API_KEY=...     # 없으면 후보 + evidence 포함 프롬프트 저장
  python recommend_user.py --persona p2-senior-backend --topn 20 --k 5
"""
from __future__ import annotations
import argparse, json, os, sys
import psycopg2
from psycopg2.extras import RealDictCursor
from fastembed import TextEmbedding
import retrieve_user as R
import llm_select as S


def load_evidence(conn, uid):
    """LLM 선별기에 줄 근거 묶음: 신호②(이력서·스킬) + 신호④(pref_memory, 진화형 선호)."""
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        cur.execute("SELECT raw_text, parsed FROM user_documents "
                    "WHERE user_id=%s AND doc_type='resume' AND is_active "
                    "ORDER BY uploaded_at DESC LIMIT 1", (uid,))
        doc = cur.fetchone()
        cur.execute("SELECT skill, evidence FROM user_skills "
                    "WHERE user_id=%s AND source='resume' ORDER BY skill", (uid,))
        skills = cur.fetchall()
        cur.execute("SELECT pref_memory FROM user_signal_state WHERE user_id=%s", (uid,))
        st = cur.fetchone()
    pref_memory = (st or {}).get("pref_memory")
    if not doc and not pref_memory:
        return None
    parsed = (doc or {}).get("parsed") or {}
    return {
        "career_years": parsed.get("career_years"),
        "resume_summary": ((doc or {}).get("raw_text") or "")[:700],
        "skills_with_evidence": [{"skill": s["skill"], "evidence": s["evidence"]} for s in skills],
        "learned_preferences": pref_memory,   # 신호④: 피드백 요약(진화형 선호)
    }


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument("--personas", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"))
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    ap.add_argument("--topn", type=int, default=20)
    ap.add_argument("--overfetch", type=int, default=200)
    ap.add_argument("--k", type=int, default=5)
    ap.add_argument("--model", default=S.MODEL)
    a = ap.parse_args()
    if not a.dsn:
        sys.exit("DATABASE_URL(또는 --dsn) 필요")
    personas = {p["id"]: p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas[a.persona]

    conn = psycopg2.connect(a.dsn)
    model = TextEmbedding(model_name=R.MODEL)
    info, cands = R.retrieve_user(persona["id"], conn, model, a.topn, a.overfetch)
    evidence = load_evidence(conn, info["uid"])
    conn.close()
    print(f"[1/2] 후보 {len(cands)}건 | 신호 resume={info['resume']} taste={info['taste']} "
          f"canonical_skills={info['n_uskills']} blend={info['weights']}", file=sys.stderr)
    json.dump({"persona": persona, "signals": info, "candidates": cands},
              open(f"candidates_user_{a.persona}.json", "w"), ensure_ascii=False, indent=2)

    try:
        result = S.select(persona, cands, a.k, a.model, evidence=evidence)
    except RuntimeError as e:
        prompt = S.SYS + "\n\n" + S.build_prompt(persona, cands, evidence).replace("{K}", str(a.k))
        open(f"select_prompt_user_{a.persona}.txt", "w").write(prompt)
        print(f"[2/2] LLM 키 없음({e}) → select_prompt_user_{a.persona}.txt 저장. 에이전트/콘솔에서 선별.",
              file=sys.stderr)
        return
    json.dump(result, open(f"reco_user_{a.persona}.json", "w"), ensure_ascii=False, indent=2)
    print(f"\n=== [{persona['label']}] 추천 top-{a.k} (신호통합) ===")
    for r in result.get("recommendations", []):
        print(f"  #{r.get('rank')} {r.get('position')} @ {r.get('company')}\n     → {r.get('reason')}")
    print(f"→ reco_user_{a.persona}.json")


if __name__ == "__main__":
    main()
