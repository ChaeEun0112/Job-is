"""LLM 최종 선별기: 후보군 → 프로필 대조 → top-K 공고 선별 + 추천 이유.

retrieve(임베딩+정형필터)가 넓게 뽑은 후보군에서, LLM(Claude)이 사용자 프로필과
대조해 **진짜 맞는 top-K 만 선별하고 이유를 생성**한다. (recall은 임베딩, precision은 LLM)

- stdlib urllib 로 Anthropic Messages API 호출 (SDK 불필요). ANTHROPIC_API_KEY 필요.
- 구조화 출력(JSON) 강제 → 파싱 안정.

사용:
  export ANTHROPIC_API_KEY=...
  python select.py --candidates candidates_p2-senior-backend.json --k 5
"""
from __future__ import annotations
import argparse, json, os, sys, urllib.request

API = "https://api.anthropic.com/v1/messages"
MODEL = "claude-sonnet-5"   # rerank/선별 — 비용/품질 균형. --model 로 변경

SYS = (
    "너는 Job.is의 취업 큐레이터다. 아래 사용자 프로필과, 임베딩+정형필터로 1차 선별된 "
    "채용공고 후보군을 받는다. 후보 중에서 이 사용자에게 **진짜 적합한** 공고만 골라 "
    "top-K로 순위를 매기고, 각 공고에 대해 프로필↔공고 근거(직무·기술·경력·기업규모·지역)를 "
    "짧고 구체적으로 설명한다. 억지로 채우지 말 것 — 약한 후보는 제외하고 이유를 남겨라. "
    "과장 금지, 근거 중심. 반드시 아래 JSON 스키마로만 답한다(설명 텍스트 없이 JSON만)."
)
SCHEMA = {
    "recommendations": [
        {"rank": 1, "external_id": 0, "position": "", "company": "",
         "reason": "한 문장 핵심 추천 이유", "fit_points": ["근거1", "근거2"],
         "caution": "주의점(있으면, 없으면 빈 문자열)"}
    ],
    "dropped_note": "후보 중 뺀 것들에 대한 짧은 총평(선택)",
}


def build_prompt(persona, candidates, evidence=None):
    cand_slim = [{
        "external_id": c["external_id"], "position": c["position"], "company": c["company"],
        "company_type": c.get("company_type"), "employee_count": c.get("employee_count"),
        "location": c.get("location_full"), "is_remote": c.get("is_remote"),
        "career_min": c.get("career_min"), "career_max": c.get("career_max"),
        "skills": c.get("skill_tags"), "skills_inferred": c.get("skills_inferred"),
        "requirements": c.get("req_snippet"), "tasks": c.get("tasks_snippet"),
    } for c in candidates]
    # 신호② 이력서/역량 근거(있으면) — '근거 있는 추천 이유' 생성용 (SIGNAL_DESIGN §4)
    ev_block = (f"[역량/이력서 근거]\n{json.dumps(evidence, ensure_ascii=False, indent=2)}\n"
                "위 근거가 있으면 추천 이유를 반드시 '지원자의 X 경험/스킬이 공고의 Y 요구와 맞다' 형태로 "
                "구체적으로 연결하라.\n\n") if evidence else ""
    return (
        f"[사용자 프로필]\n{json.dumps(persona, ensure_ascii=False, indent=2)}\n\n"
        f"{ev_block}"
        f"[후보 공고군 {len(cand_slim)}건]\n{json.dumps(cand_slim, ensure_ascii=False, indent=2)}\n\n"
        f"[출력 JSON 스키마]\n{json.dumps(SCHEMA, ensure_ascii=False)}\n\n"
        f"top-{{K}} 를 선별해 위 스키마의 JSON 으로만 답하라."
    )


def call_claude(prompt, k, model, api_key):
    body = json.dumps({
        "model": model, "max_tokens": 2000,
        "system": SYS,
        "messages": [{"role": "user", "content": prompt.replace("{K}", str(k))}],
    }).encode()
    req = urllib.request.Request(API, data=body, headers={
        "x-api-key": api_key, "anthropic-version": "2023-06-01",
        "content-type": "application/json"})

    try:
        with urllib.request.urlopen(req, timeout=90) as r:
            resp = json.loads(r.read())
        text = "".join(b.get("text", "") for b in resp.get("content", []))
        text = text.strip().removeprefix("```json").removeprefix("```").removesuffix("```").strip()
        return json.loads(text)
    except Exception as e:
        raise RuntimeError(f"Claude API 호출 또는 응답 파싱 실패: {e}") from e


def select(persona, candidates, k=5, model=MODEL, api_key=None, evidence=None):
    api_key = api_key or os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        raise RuntimeError("ANTHROPIC_API_KEY 필요 (또는 candidates JSON을 에이전트/콘솔에서 선별)")
    return call_claude(build_prompt(persona, candidates, evidence), k, model, api_key)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--candidates", required=True)
    ap.add_argument("--k", type=int, default=5)
    ap.add_argument("--model", default=MODEL)
    ap.add_argument("--out", default=None)
    a = ap.parse_args()
    data = json.load(open(a.candidates, encoding="utf-8"))
    try:
        result = select(data["persona"], data["candidates"], a.k, a.model)
    except RuntimeError as e:
        print(f"[skip] {e}", file=sys.stderr)
        # 키 없을 때: LLM에 넣을 프롬프트를 파일로 남겨 수동/에이전트 선별 가능
        open("select_prompt.txt", "w").write(
            SYS + "\n\n" + build_prompt(data["persona"], data["candidates"]).replace("{K}", str(a.k)))
        print("select_prompt.txt 생성 — 에이전트/콘솔에 넣어 선별하세요.", file=sys.stderr)
        return
    out = a.out or a.candidates.replace("candidates_", "reco_")
    json.dump(result, open(out, "w"), ensure_ascii=False, indent=2)
    for r in result.get("recommendations", []):
        print(f"  #{r.get('rank')} {r.get('position')} @ {r.get('company')}\n     → {r.get('reason')}")
    print(f"→ {out}")


if __name__ == "__main__":
    main()
