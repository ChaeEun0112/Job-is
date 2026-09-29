"""JSONL → PostgreSQL 적재기 (백엔드 인계용).

companies 먼저(normalized_name upsert) → normalized_name→id 맵 구성 →
job_postings 적재(company_normalized_name 으로 company_id 결선, (source,external_id) upsert).

전제: psycopg2 (pip install psycopg2-binary)

사용:
  export DATABASE_URL=postgresql://user:pass@host:5432/jobis
  python load.py --dir ../out --schema ../schema.sql --init   # 스키마 생성(드롭 후) + 적재
  python load.py --dir ../out                                 # 적재만(테이블 존재 전제, upsert)
"""
from __future__ import annotations
import argparse, json, os, sys
import psycopg2
from psycopg2.extras import execute_values, Json

COMPANY_COLS = ["name", "normalized_name", "registration_number", "wanted_company_id",
                "jobkorea_gno_ref", "employee_count", "company_type", "industry",
                "stock_status", "hq_address", "homepage", "description",
                "enrichment_status", "name_match", "rejected_name", "raw_jobkorea"]

POSTING_COLS = ["source", "external_id", "source_url", "company_id", "position", "intro",
                "main_tasks", "requirements", "preferred_points", "benefits",
                "category_parent", "category_child", "career_min", "career_max",
                "is_newbie", "is_expert", "employment_type", "location_country",
                "location_city", "location_district", "location_full", "geo_lat", "geo_lng",
                "is_remote", "due_time", "confirm_time", "status", "hire_rounds",
                "skill_tags", "skill_tag_ids", "skills_inferred", "thumbnail_url",
                "image_urls", "reward_total", "raw"]

JSONB = {"raw_jobkorea", "raw"}


def rows(path):
    with open(path, encoding="utf-8") as f:
        return [json.loads(l) for l in f if l.strip()]


def val(rec, col):
    v = rec.get(col)
    return Json(v) if col in JSONB and v is not None else v


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dir", default="../out", help="companies.jsonl/job_postings.jsonl 위치")
    ap.add_argument("--schema", default="../schema.sql")
    ap.add_argument("--init", action="store_true", help="테이블 드롭 후 재생성")
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    a = ap.parse_args()
    if not a.dsn:
        sys.exit("DATABASE_URL(또는 --dsn) 필요")

    comps = rows(os.path.join(a.dir, "companies.jsonl"))
    posts = rows(os.path.join(a.dir, "job_postings.jsonl"))
    conn = psycopg2.connect(a.dsn)
    conn.autocommit = False
    cur = conn.cursor()

    if a.init:
        cur.execute("DROP TABLE IF EXISTS job_postings, companies CASCADE;")
        cur.execute(open(a.schema, encoding="utf-8").read())
        print("[init] 스키마 생성 완료")

    # 1) companies upsert
    cset = ", ".join(c for c in COMPANY_COLS if c != "normalized_name")
    updates = ", ".join(f"{c}=EXCLUDED.{c}" for c in COMPANY_COLS if c != "normalized_name")
    execute_values(
        cur,
        f"INSERT INTO companies ({', '.join(COMPANY_COLS)}) VALUES %s "
        f"ON CONFLICT (normalized_name) DO UPDATE SET {updates}, updated_at=now()",
        [[val(c, col) for col in COMPANY_COLS] for c in comps],
    )
    # 2) normalized_name → id
    cur.execute("SELECT normalized_name, id FROM companies")
    idmap = dict(cur.fetchall())

    # 3) postings upsert (company_id 결선)
    prepared = []
    missing = 0
    for p in posts:
        cid = idmap.get(p.get("company_normalized_name"))
        if cid is None:
            missing += 1
        rec = dict(p)
        rec["company_id"] = cid
        prepared.append([val(rec, col) for col in POSTING_COLS])
    p_updates = ", ".join(f"{c}=EXCLUDED.{c}" for c in POSTING_COLS if c not in ("source", "external_id"))
    execute_values(
        cur,
        f"INSERT INTO job_postings ({', '.join(POSTING_COLS)}) VALUES %s "
        f"ON CONFLICT (source, external_id) DO UPDATE SET {p_updates}",
        prepared,
    )
    conn.commit()

    cur.execute("SELECT count(*) FROM companies")
    nc = cur.fetchone()[0]
    cur.execute("SELECT count(*) FROM job_postings")
    npq = cur.fetchone()[0]
    print(f"[load] companies {nc}행 / job_postings {npq}행 (company_id 미결선 {missing}건)")
    cur.close(); conn.close()


if __name__ == "__main__":
    main()
