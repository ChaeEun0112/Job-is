"""오케스트레이션: 원티드 목록 → 공고 수집 → (기업별) 잡코리아 메타 보강 → JSONL 출력.

출력:
  companies.jsonl     : 기업 1행씩 (잡코리아 메타 보강 포함)
  job_postings.jsonl  : 공고 1행씩 (company_normalized_name 으로 companies 와 조인)

DB 적재: schema.sql 로 테이블 생성 후, JSONL 을 COPY/INSERT (가이드 참고).

사용:
  python run_pipeline.py --target 30 --per-tag 20 --delay 1.5 --out ../out
"""
from __future__ import annotations
import argparse
import json
import os
import sys
import time

from wanted import list_dev_job_ids, fetch_posting
from jobkorea_company import enrich


def run(target, per_tag, delay, out_dir):
    os.makedirs(out_dir, exist_ok=True)
    companies = {}          # normalized_name -> 기업 레코드
    postings = []

    # 목록 단계에서 개발 세부직군 태그로만 발견(사전 fetch 낭비 제거).
    # target 의 3배까지 후보 확보(태그 누수분 + 중복 여유).
    pool = list_dev_job_ids(per_tag=per_tag, max_total=target * 3 if target else None)
    print(f"[discover] 개발 후보 {len(pool)}건 → 목표 {target or '전체'}건 추출", file=sys.stderr)

    for jid in pool:
        if target and len(postings) >= target:
            break
        try:
            p = fetch_posting(jid)
        except Exception as e:
            print(f"[wanted] {jid} skip: {e}", file=sys.stderr)
            continue
        # 태그가 깨끗하지만 누수 가능 → 개발 직군 최종 가드(이제 저렴)
        if p.get("category_parent") != "개발":
            continue
        postings.append(p)
        print(f"  + [{len(postings)}] {jid}: {p['position']} @ {p['company']['name']} "
              f"(skill {len(p['skill_tags'])})", file=sys.stderr)

        cn = p["company"]["normalized_name"]
        if cn and cn not in companies:
            comp = dict(p["company"])
            try:
                meta = enrich(p["company"]["name"])   # 잡코리아 보강
            except Exception as e:
                meta = {"enrichment_status": "not_found", "error": str(e)}
            # 원티드 기본 + 잡코리아 메타 병합 (잡코리아 industry 가 있으면 우선)
            comp["industry"] = meta.get("industry") or comp.get("industry_name")
            for k in ("jobkorea_gno_ref", "employee_count", "company_type",
                      "stock_status", "hq_address", "name_match",
                      "enrichment_status", "raw_jobkorea"):
                if k in meta:
                    comp[k] = meta[k]
            comp.setdefault("enrichment_status", "not_found")
            companies[cn] = comp
            st = comp.get("enrichment_status")
            print(f"    └ 기업메타[{st}]: {comp.get('company_type')}/"
                  f"{comp.get('employee_count')}명/{comp.get('stock_status')}", file=sys.stderr)
            time.sleep(delay)
        time.sleep(delay)

    with open(os.path.join(out_dir, "companies.jsonl"), "w", encoding="utf-8") as f:
        for c in companies.values():
            f.write(json.dumps(c, ensure_ascii=False) + "\n")
    with open(os.path.join(out_dir, "job_postings.jsonl"), "w", encoding="utf-8") as f:
        for p in postings:
            p["company_normalized_name"] = p["company"]["normalized_name"]
            f.write(json.dumps(p, ensure_ascii=False) + "\n")

    enriched = sum(1 for c in companies.values() if c.get("enrichment_status") == "enriched")
    print(f"\n완료: 공고 {len(postings)}건 / 기업 {len(companies)}곳 "
          f"(잡코리아 보강 성공 {enriched}곳) → {out_dir}/")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--target", type=int, default=30, help="추출할 개발 공고 목표 수 (0=후보 전체)")
    ap.add_argument("--per-tag", type=int, default=20, help="세부직군 태그당 목록 수")
    ap.add_argument("--delay", type=float, default=1.5)
    ap.add_argument("--out", default="../out")
    a = ap.parse_args()
    run(a.target, a.per_tag, a.delay, a.out)
