"""샤드 추출기(멀티에이전트 1워커). 샤드 id 목록 → 공고 수집 + 기업 보강 → 샤드 JSONL.

사용:
  python shard_extract.py --shard ../out/shards/shard_0.json --cap 100 \
      --delay 1.2 --label 0 --out ../out/shards
"""
from __future__ import annotations
import argparse, json, os, sys, time
from wanted import fetch_posting
from jobkorea_company import enrich

META_KEYS = ("jobkorea_gno_ref", "employee_count", "company_type", "stock_status",
             "hq_address", "name_match", "enrichment_status", "raw_jobkorea", "rejected_name")


def run(shard_file, cap, delay, out_dir, label, max_candidates):
    ids = json.load(open(shard_file))
    companies, postings = {}, []
    for jid in ids:
        if cap and len(postings) >= cap:
            break
        try:
            p = fetch_posting(jid)
        except Exception as e:
            print(f"[{label}] {jid} skip: {e}", file=sys.stderr)
            continue
        if p.get("category_parent") != "개발":      # 태그 누수 최종 가드
            continue
        postings.append(p)
        cn = p["company"]["normalized_name"]
        if cn and cn not in companies:
            comp = dict(p["company"])
            try:
                meta = enrich(p["company"]["name"], max_candidates=max_candidates)
            except Exception as e:
                meta = {"enrichment_status": "not_found", "error": str(e)}
            comp["industry"] = meta.get("industry") or comp.get("industry_name")
            for k in META_KEYS:
                if k in meta:
                    comp[k] = meta[k]
            comp.setdefault("enrichment_status", "not_found")
            companies[cn] = comp
            time.sleep(delay)
        if len(postings) % 20 == 0:
            print(f"[{label}] {len(postings)}건", file=sys.stderr)
        time.sleep(delay)

    os.makedirs(out_dir, exist_ok=True)
    pp = os.path.join(out_dir, f"postings_{label}.jsonl")
    cp = os.path.join(out_dir, f"companies_{label}.jsonl")
    with open(pp, "w", encoding="utf-8") as f:
        for p in postings:
            p["company_normalized_name"] = p["company"]["normalized_name"]
            f.write(json.dumps(p, ensure_ascii=False) + "\n")
    with open(cp, "w", encoding="utf-8") as f:
        for c in companies.values():
            f.write(json.dumps(c, ensure_ascii=False) + "\n")
    enr = sum(1 for c in companies.values() if c.get("enrichment_status") == "enriched")
    print(f"[{label}] DONE 공고 {len(postings)} / 기업 {len(companies)} (enriched {enr}) → {pp}")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--shard", required=True)
    ap.add_argument("--cap", type=int, default=100)
    ap.add_argument("--delay", type=float, default=1.2)
    ap.add_argument("--label", required=True)
    ap.add_argument("--out", default="../out/shards")
    ap.add_argument("--max-candidates", type=int, default=3)
    a = ap.parse_args()
    run(a.shard, a.cap, a.delay, a.out, a.label, a.max_candidates)
