"""개발 세부직군 태그를 offset 페이징으로 훑어 후보 id 풀을 구성하고 N개 샤드로 분할.

출력:
  <out>/pool.json            : 전체 후보 id (순서보존 dedup)
  <out>/shard_{k}.json       : k번째 샤드 id 목록 (멀티에이전트용)

사용:
  python build_pool.py --shards 10 --per-tag-max 400 --out ../out/shards
"""
from __future__ import annotations
import argparse, json, os, sys, time
from wanted import list_job_ids, DEV_TAG_IDS


def build_pool(per_tag_max, page=100, delay=0.4):
    seen, pool = set(), []
    for tid in DEV_TAG_IDS:
        got = 0
        for off in range(0, per_tag_max, page):
            try:
                ids = list_job_ids(limit=page, offset=off, tag_type_id=tid)
            except Exception as e:
                print(f"[tag {tid}] off {off} 실패: {e}", file=sys.stderr)
                break
            if not ids:
                break
            new = 0
            for j in ids:
                if j not in seen:
                    seen.add(j); pool.append(j); new += 1
            got += len(ids)
            time.sleep(delay)
            if len(ids) < page:
                break
        print(f"[tag {tid}] 누적 풀 {len(pool)}", file=sys.stderr)
    return pool


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--shards", type=int, default=10)
    ap.add_argument("--per-tag-max", type=int, default=400)
    ap.add_argument("--out", default="../out/shards")
    a = ap.parse_args()
    os.makedirs(a.out, exist_ok=True)
    pool = build_pool(a.per_tag_max)
    json.dump(pool, open(os.path.join(a.out, "pool.json"), "w"))
    # 라운드로빈 분할(인접 id 가 같은 샤드에 몰리지 않게)
    shards = [[] for _ in range(a.shards)]
    for i, jid in enumerate(pool):
        shards[i % a.shards].append(jid)
    for k, sh in enumerate(shards):
        json.dump(sh, open(os.path.join(a.out, f"shard_{k}.json"), "w"))
    print(f"\n풀 {len(pool)}건 → {a.shards}개 샤드 (각 ~{len(pool)//a.shards}건) → {a.out}/")
