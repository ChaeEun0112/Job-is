"""엔드투엔드 오케스트레이터: 프로필 → (임베딩+정형) 후보검색 → LLM 최종선별 → 추천 top-K.

  python recommend.py --persona p2-senior-backend --topn 20 --k 5

ANTHROPIC_API_KEY 있으면 LLM 선별까지 자동. 없으면 후보군 + 프롬프트만 저장(에이전트로 선별).
"""
from __future__ import annotations
import argparse, json, os, sys
import numpy as np
from fastembed import TextEmbedding
import retrieve as R
import llm_select as S


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument("--personas", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"))
    ap.add_argument("--data", default=".")
    ap.add_argument("--topn", type=int, default=20)
    ap.add_argument("--k", type=int, default=5)
    ap.add_argument("--model", default=S.MODEL)
    a = ap.parse_args()

    personas = {p["id"]: p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas[a.persona]
    vecs, meta = R.load(a.data)
    model = TextEmbedding(model_name=R.MODEL)

    # 1) 후보 검색 (recall)
    cands = R.retrieve(persona, vecs, meta, model, a.topn)
    print(f"[1/2] 후보 {len(cands)}건 검색 (임베딩+정형필터)", file=sys.stderr)
    json.dump({"persona": persona, "candidates": cands},
              open(f"candidates_{a.persona}.json", "w"), ensure_ascii=False, indent=2)

    # 2) LLM 최종 선별 (precision)
    try:
        result = S.select(persona, cands, a.k, a.model)
    except RuntimeError as e:
        prompt = S.SYS + "\n\n" + S.build_prompt(persona, cands).replace("{K}", str(a.k))
        open(f"select_prompt_{a.persona}.txt", "w").write(prompt)
        print(f"[2/2] LLM 키 없음({e}) → select_prompt_{a.persona}.txt 로 저장. 에이전트/콘솔에서 선별.",
              file=sys.stderr)
        return
    json.dump(result, open(f"reco_{a.persona}.json", "w"), ensure_ascii=False, indent=2)
    print(f"\n=== [{persona['label']}] 추천 top-{a.k} ===")
    for r in result.get("recommendations", []):
        print(f"  #{r.get('rank')} {r.get('position')} @ {r.get('company')}")
        print(f"     → {r.get('reason')}")
    print(f"→ reco_{a.persona}.json")


if __name__ == "__main__":
    main()
