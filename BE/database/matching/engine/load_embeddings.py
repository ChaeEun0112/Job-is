"""공고 임베딩(embeddings.npy) → job_postings.embedding(pgvector) 적재.

embed.py 가 만든 embeddings.npy[i] ↔ meta.jsonl[i] 는 external_id 로 매칭된다.
DB 화 1단계: matching 의 로컬 npy 임베딩을 pgvector 컬럼으로 이관 → SQL KNN 매칭 준비.

  export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis
  python load_embeddings.py --data . --hnsw
"""
from __future__ import annotations
import argparse, json, os, sys
import numpy as np
import psycopg2
from psycopg2.extras import execute_values


def vec_literal(row) -> str:
    # pgvector 입력 포맷: '[0.1,0.2,...]'
    return "[" + ",".join(f"{x:.6f}" for x in row) + "]"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--data", default=".", help="embeddings.npy / meta.jsonl 위치")
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    ap.add_argument("--source", default="wanted")
    ap.add_argument("--hnsw", action="store_true", help="적재 후 HNSW 코사인 인덱스 생성")
    a = ap.parse_args()
    if not a.dsn:
        sys.exit("DATABASE_URL(또는 --dsn) 필요")

    vecs = np.load(os.path.join(a.data, "embeddings.npy"))
    meta = [json.loads(l) for l in open(os.path.join(a.data, "meta.jsonl"), encoding="utf-8") if l.strip()]
    if len(vecs) != len(meta):
        sys.exit(f"길이 불일치: embeddings {len(vecs)} vs meta {len(meta)}")
    print(f"임베딩 {len(vecs)}건 x {vecs.shape[1]}d → source={a.source}")

    rows = [(a.source, m["external_id"], vec_literal(v)) for m, v in zip(meta, vecs)]

    conn = psycopg2.connect(a.dsn)
    conn.autocommit = False
    cur = conn.cursor()

    # 단일 라운드트립 벌크 UPDATE (source+external_id 매칭)
    # RETURNING으로 실제 갱신된 행 ID를 명시적으로 반환 (page_size로 나뉠 때도 정확함)
    updated_ids = execute_values(
        cur,
        """UPDATE job_postings AS jp
           SET embedding = v.emb::vector
           FROM (VALUES %s) AS v(src, ext, emb)
           WHERE jp.source = v.src AND jp.external_id = v.ext
           RETURNING jp.id""",
        rows, template="(%s, %s, %s)", page_size=500, fetch=True)

    # 실제 갱신된 행 수 검증 (0건 갱신 시 에러 처리)
    updated_count = len(updated_ids) if updated_ids else 0
    if updated_count == 0:
        conn.rollback()
        sys.exit(f"[ERROR] 갱신된 행이 0개입니다. --source('{a.source}') 값이나 external_id 매칭 여부를 확인하세요.")
    conn.commit()

    cur.execute("SELECT count(*) FROM job_postings WHERE embedding IS NOT NULL")
    n_emb = cur.fetchone()[0]
    cur.execute("SELECT count(*) FROM job_postings")
    n_tot = cur.fetchone()[0]
    print(f"[embed] {n_emb}/{n_tot} 행에 embedding 적재됨 (이번 실행으로 {updated_count}건 갱신)")


    if a.hnsw:
        cur.execute("CREATE INDEX IF NOT EXISTS idx_jp_emb "
                    "ON job_postings USING hnsw (embedding vector_cosine_ops)")
        conn.commit()
        print("[embed] HNSW 코사인 인덱스 생성됨 (idx_jp_emb)")
    cur.close(); conn.close()


if __name__ == "__main__":
    main()
