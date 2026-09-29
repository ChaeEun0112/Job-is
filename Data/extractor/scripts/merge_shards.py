"""샤드 결과 병합 + dedup → 최종 out/job_postings.jsonl, out/companies.jsonl + 통계.

  공고 : external_id 기준 dedup
  기업 : normalized_name 기준 dedup (enriched 를 우선 채택)

사용:
  python merge_shards.py --shards-dir ../out/shards --out ../out
"""
from __future__ import annotations
import argparse, glob, json, os
from collections import Counter

RANK = {"enriched": 3, "name_mismatch": 2, "not_found": 1, None: 0}


def main(shards_dir, out_dir):
    posts, comps = {}, {}
    for fp in sorted(glob.glob(os.path.join(shards_dir, "postings_*.jsonl"))):
        for line in open(fp, encoding="utf-8"):
            p = json.loads(line)
            posts.setdefault(p["external_id"], p)            # 첫 등장 유지
    for fp in sorted(glob.glob(os.path.join(shards_dir, "companies_*.jsonl"))):
        for line in open(fp, encoding="utf-8"):
            c = json.loads(line)
            k = c["normalized_name"]
            cur = comps.get(k)
            # 더 좋은 보강상태를 우선
            if cur is None or RANK.get(c.get("enrichment_status"), 0) > RANK.get(cur.get("enrichment_status"), 0):
                comps[k] = c

    os.makedirs(out_dir, exist_ok=True)
    with open(os.path.join(out_dir, "job_postings.jsonl"), "w", encoding="utf-8") as f:
        for p in posts.values():
            f.write(json.dumps(p, ensure_ascii=False) + "\n")
    with open(os.path.join(out_dir, "companies.jsonl"), "w", encoding="utf-8") as f:
        for c in comps.values():
            f.write(json.dumps(c, ensure_ascii=False) + "\n")

    np = len(posts); nc = len(comps)
    st = Counter(c.get("enrichment_status") for c in comps.values())
    sk = sum(1 for p in posts.values() if p.get("skill_tags"))
    th = sum(1 for p in posts.values() if p.get("thumbnail_url"))
    print(f"=== 병합 완료 → {out_dir}/ ===")
    print(f"공고 {np}건  (skill {sk}={100*sk//np}% / thumbnail {th}={100*th//np}%)")
    print(f"기업 {nc}곳  보강: " + " ".join(f"{k}={v}" for k, v in st.most_common()))
    print(f"  enriched 비율: {100*st.get('enriched',0)//nc}%")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--shards-dir", default="../out/shards")
    ap.add_argument("--out", default="../out")
    a = ap.parse_args()
    main(a.shards_dir, a.out)
