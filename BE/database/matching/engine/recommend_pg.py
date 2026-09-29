"""엔드투엔드 (pgvector DB 백엔드): 프로필 → SQL KNN 후보검색 → LLM 최종선별 → top-K.

recommend.py 의 DB화 버전. 후보검색만 numpy → pgvector 로 바뀌고, LLM 선별(llm_select)은 동일.

  export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis
  export ANTHROPIC_API_KEY=...            # 없으면 후보 + 프롬프트만 저장
  python recommend_pg.py --persona p2-senior-backend --topn 20 --k 5
"""
from __future__ import annotations
import argparse, json, os, sys
import psycopg2
from fastembed import TextEmbedding
import retrieve_pg as R
import llm_select as S


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

    # 1) 후보 검색 (recall) — pgvector KNN + 하드필터 + 소프트부스트
    conn = psycopg2.connect(a.dsn)
    model = TextEmbedding(model_name=R.MODEL)
    cands = R.retrieve_pg(persona, conn, model, a.topn, a.overfetch)
    conn.close()
    print(f"[1/2] 후보 {len(cands)}건 검색 (pgvector KNN + 정형필터)", file=sys.stderr)
    json.dump({"persona": persona, "candidates": cands},
              open(f"candidates_pg_{a.persona}.json", "w", encoding="utf-8"), ensure_ascii=False, indent=2)

    # 2) LLM 최종 선별 (precision) — llm_select 그대로
    try:
        result = S.select(persona, cands, a.k, a.model)
    except RuntimeError as e:
        prompt = S.SYS + "\n\n" + S.build_prompt(persona, cands).replace("{K}", str(a.k))
        open(f"select_prompt_pg_{a.persona}.txt", "w").write(prompt)
        print(f"[2/2] LLM 키 없음({e}) → select_prompt_pg_{a.persona}.txt 저장. 에이전트/콘솔에서 선별.",
              file=sys.stderr)
        return
    json.dump(result, open(f"reco_pg_{a.persona}.json", "w"), ensure_ascii=False, indent=2)
    print(f"\n=== [{persona['label']}] 추천 top-{a.k} (pgvector) ===")
    for r in result.get("recommendations", []):
        print(f"  #{r.get('rank')} {r.get('position')} @ {r.get('company')}")
        print(f"     → {r.get('reason')}")
    print(f"→ reco_pg_{a.persona}.json")


if __name__ == "__main__":
    main()
