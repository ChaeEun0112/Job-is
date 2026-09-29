"""Editor's Note 생성: 공고 1건 → 큐레이션 코멘트 1줄 (DET-01 ⑤, jobs.editor_note).

크롤 파이프라인(run_pipeline.py) 이후, DB 적재 이전에 실행하는 보강 단계다.
--enrich 로 만든 JSONL(원본 + editor_note 키)을 기존 공고 적재 경로가 그대로 읽으면
jobs.editor_note 에 저장되고, 상세 API 응답(editorNote)으로 노출된다. null 이면
화면에서 섹션 자체가 비노출(정상). 공고당 1개라 사용자·추천 파이프라인과 무관하다.

전송 계층은 --provider 로 교체 가능(프롬프트 공통):
  claude-cli  개발용. 로그인된 Claude 구독으로 호출(claude -p). API 키 불필요.
  openai      운영 최종. OPENAI_API_KEY 필요.

사용:
  python editor_note.py --jobs ../out/job_postings.jsonl --companies ../out/companies.jsonl \
      --provider openai --out editor_notes.json --enrich ../out/job_postings_noted.jsonl
"""
from __future__ import annotations
import argparse, json, os, subprocess, sys, urllib.request

OPENAI_API = "https://api.openai.com/v1/chat/completions"
MODEL = "claude-sonnet-5"
OPENAI_MODEL = "gpt-5.5"

SYS = (
    "너는 취업 추천 서비스 Job.is의 에디터다. 채용공고 원문을 읽고, 상세 화면의 "
    "'Editor's Note(비서의 노트)' — 이 공고에서 눈에 띄는 점과 어떤 분께 맞을지를 짚어주는 "
    "짧은 큐레이션 코멘트 — 를 쓴다. 반드시 JSON으로만 답한다.\n"
    "작성 규칙(위반 시 노출 불가):\n"
    "1) 공고·기업 소개 원문에 명시된 사실만 쓴다. 원문에 없는 조직문화·성장기회·분위기를 "
    "만들어내지 말 것.\n"
    "2) 과장·단정·광고 문구 금지('완벽한', '무조건', '놓치면 안 될', '최고의'). 담백하게.\n"
    "3) 이 노트는 모든 사용자에게 동일하게 노출된다. 독자의 조건을 아는 척하지 말 것 "
    "('당신의 3년 경력' 금지). '~을 찾는 분이라면' 같은 조건형 표현은 허용.\n"
    "4) skills_inferred=true면 skills 배열은 추정 태그다. 원문(요건·업무)에 등장하는 "
    "스킬만 언급할 것.\n"
    "5) 계약직·파견·상주 등 유의점이 원문에 있으면 숨기지 말고 자연스럽게 짚는다.\n"
    "6) 정확히 1~2문장(3문장 금지), 전체 80~150자, 존댓말(…합니다체). 내부 필드명을 "
    "그대로 노출하지 말 것.\n"
    "7) 특기할 근거가 없으면 note를 null로 답하라. 억지로 쓰는 것이 가장 나쁘다.\n"
    "8) 원문에서 별개 항목으로 나열된 요건을 한 문장으로 합쳐 원문에 없는 복합 조건을 "
    "만들지 말 것(예: '검색개발 5년'과 'Elasticsearch 경험'을 'Elasticsearch 5년'으로 "
    "결합 금지). 경력 연수는 원문이 붙인 대상 그대로 쓴다.\n"
    "9) 외부 지식을 보태지 말 것. 주소를 원문에 없는 지역명으로 바꿔 부르거나(예: "
    "'가산 사무실'), 업계 관행으로 근무 형태를 추론(예: '고객사 상주')하지 말 것."
)
SCHEMA = {"editor_note": "코멘트 (근거 부족 시 null)", "skip_reason": "null일 때 이유(선택)"}


def build_prompt(job, company=None):
    slim = {k: job.get(k) for k in (
        "position", "employment_type", "career_min", "career_max", "is_newbie",
        "location_full", "is_remote", "intro", "main_tasks", "requirements",
        "preferred_points", "benefits", "skill_tags", "skills_inferred")}
    slim["company"] = job.get("company")
    if company:
        slim["company_industry"] = company.get("industry_name")
        desc = company.get("description") or ""
        slim["company_description"] = desc[:600] or None
    return (
        f"[채용공고]\n{json.dumps(slim, ensure_ascii=False, indent=2)}\n\n"
        f"[출력 JSON 스키마]\n{json.dumps(SCHEMA, ensure_ascii=False)}\n\n"
        "위 공고의 Editor's Note를 스키마 JSON으로만 답하라."
    )


def _extract_json(text):
    text = text.strip().removeprefix("```json").removeprefix("```").removesuffix("```").strip()
    return json.loads(text)


def call_openai(prompt, model, api_key):
    body = json.dumps({
        "model": model,
        "messages": [{"role": "system", "content": SYS},
                     {"role": "user", "content": prompt}],
        "response_format": {"type": "json_object"},
    }).encode()
    req = urllib.request.Request(OPENAI_API, data=body, headers={
        "authorization": f"Bearer {api_key}", "content-type": "application/json"})
    with urllib.request.urlopen(req, timeout=120) as r:
        resp = json.loads(r.read())
    u = resp.get("usage") or {}
    print(f"[openai usage] in={u.get('prompt_tokens')} out={u.get('completion_tokens')} "
          f"(reasoning={(u.get('completion_tokens_details') or {}).get('reasoning_tokens')})",
          file=sys.stderr)
    return _extract_json(resp["choices"][0]["message"]["content"])


def call_claude_cli(prompt, model):
    proc = subprocess.run(
        ["claude", "-p", prompt, "--system-prompt", SYS, "--model", model,
         "--output-format", "json", "--allowed-tools", "",
         "--setting-sources", "", "--strict-mcp-config"],
        capture_output=True, text=True, timeout=600)
    if proc.returncode != 0:
        raise RuntimeError(f"claude exit {proc.returncode}: {proc.stderr[-300:]}")
    env = json.loads(proc.stdout)
    if env.get("is_error"):
        raise RuntimeError(f"claude api_error: {env.get('api_error_status')}")
    return _extract_json(env.get("result") or "")


def generate(job, company=None, model=MODEL, provider="claude-cli"):
    prompt = build_prompt(job, company)
    if provider == "openai":
        key = os.environ.get("OPENAI_API_KEY")
        if not key:
            raise RuntimeError("OPENAI_API_KEY 필요")
        return call_openai(prompt, OPENAI_MODEL if model.startswith("claude") else model, key)
    return call_claude_cli(prompt, model)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jobs", required=True)
    ap.add_argument("--companies", default=None)
    ap.add_argument("--sample", type=int, default=0, help="N건만 균등 샘플(0=전체)")
    ap.add_argument("--model", default=MODEL)
    ap.add_argument("--provider", default="claude-cli", choices=["claude-cli", "openai"])
    ap.add_argument("--out", default="editor_notes.json")
    ap.add_argument("--enrich", default=None,
                    help="공고 JSONL에 editor_note 키를 추가한 사본을 이 경로에 쓴다(BE 적재용)")
    a = ap.parse_args()

    jobs = [json.loads(l) for l in open(a.jobs, encoding="utf-8")]
    comps = {}
    if a.companies:
        for l in open(a.companies, encoding="utf-8"):
            c = json.loads(l)
            comps[c["normalized_name"]] = c
    if a.sample:
        step = max(1, len(jobs) // a.sample)
        jobs = jobs[::step][:a.sample]

    results = []
    for i, job in enumerate(jobs):
        comp = comps.get(job.get("company_normalized_name"))
        try:
            r = generate(job, comp, a.model, a.provider)
        except Exception as e:
            print(f"  [{i+1}/{len(jobs)}] ERROR {job['external_id']}: {e}", file=sys.stderr)
            continue
        note = r.get("editor_note")
        results.append({"external_id": job["external_id"], "position": job["position"],
                        "company": job.get("company"), "editor_note": note,
                        "skip_reason": r.get("skip_reason")})
        shown = note if note else f"(null: {r.get('skip_reason')})"
        print(f"  [{i+1}/{len(jobs)}] {job['position']} @ {job.get('company')}\n     → {shown}", flush=True)
    json.dump(results, open(a.out, "w"), ensure_ascii=False, indent=2)
    print(f"→ {a.out} ({len(results)}건)")

    if a.enrich:
        notes = {r["external_id"]: r["editor_note"] for r in results}
        with open(a.enrich, "w", encoding="utf-8") as f:
            for line in open(a.jobs, encoding="utf-8"):
                j = json.loads(line)
                j["editor_note"] = notes.get(j["external_id"])
                f.write(json.dumps(j, ensure_ascii=False) + "\n")
        print(f"→ {a.enrich} (editor_note 키 추가본)")


if __name__ == "__main__":
    main()
