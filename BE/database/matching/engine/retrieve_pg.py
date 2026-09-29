"""하이브리드 후보 검색 — pgvector DB 백엔드 버전 (retrieve.py 의 DB화).

retrieve.py 는 embeddings.npy 를 메모리에 올려 numpy 로 코사인/필터를 계산했다.
이 버전은 같은 로직을 Postgres+pgvector 로 옮긴다:
  · SQL 이 무거운 일 담당 : 벡터 KNN(ORDER BY embedding <=> q) + 하드필터(지역/경력/제외)
  · 앱은 가벼운 선호 재랭킹만 : 스킬 교집합·규모 부스트·가중합 (retrieve.py 와 동일 공식)
결과 JSON 은 retrieve.py 와 동일 스키마 → llm_select.py 그대로 사용 가능.

  export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis
  python retrieve_pg.py --persona p2-senior-backend --topn 20
"""
from __future__ import annotations
import argparse, json, os, sys
import numpy as np
import psycopg2
from psycopg2.extras import RealDictCursor
from fastembed import TextEmbedding
from retrieve import MODEL, profile_query, size_ok  # 로직 단일 출처 재사용

# SQL: 벡터 KNN + 하드필터. 소프트부스트(스킬/규모)는 앱에서 재랭킹.
SQL = """
      SELECT jp.external_id, jp.position, c.name AS company,
             c.company_type, c.employee_count, c.industry,
             jp.location_city, jp.location_full, jp.is_remote,
             jp.career_min, jp.career_max,
             jp.skill_tags, jp.skills_inferred, jp.source_url,
          left(regexp_replace(coalesce(jp.requirements,''), E'\\n', ' ', 'g'), 280) AS req_snippet,
          left(regexp_replace(coalesce(jp.main_tasks,''),  E'\\n', ' ', 'g'), 280) AS tasks_snippet,
          1 - (jp.embedding <=> %(qv)s::vector) AS score_cosine
      FROM job_postings jp
          LEFT JOIN companies c ON c.id = jp.company_id
      WHERE jp.embedding IS NOT NULL
        AND (NOT %(has_loc)s
         OR jp.location_city = ANY(%(locs)s)
         OR (%(remote_ok)s AND jp.is_remote))
        AND (%(yrs)s::int IS NULL OR jp.career_min IS NULL OR jp.career_min <= %(yrs)s)
        AND (%(nexcl)s = 0 OR NOT (
          lower(coalesce(jp.position,'') || ' ' || coalesce(c.name,'') || ' ' ||
          left(regexp_replace(coalesce(jp.requirements,''), E'\\n', ' ', 'g'), 280) || ' ' ||
          left(regexp_replace(coalesce(jp.main_tasks,''), E'\\n', ' ', 'g'), 280))
          LIKE ANY(%(excl_like)s)))
      ORDER BY jp.embedding <=> %(qv)s::vector
          LIMIT %(overfetch)s \
      """


def query_vector(persona, model) -> str:
    q = model.query_embed(profile_query(persona)) if hasattr(model, "query_embed") \
        else model.embed([profile_query(persona)])
    qv = np.array(list(q), dtype=np.float32)[0]
    qv /= (np.linalg.norm(qv) + 1e-9)
    return "[" + ",".join(f"{x:.6f}" for x in qv) + "]"


def retrieve_pg(persona, conn, model, topn=20, overfetch=200):
    locs = list(persona.get("locations") or [])
    excl = [e.lower() for e in (persona.get("exclude") or [])]
    params = {
        "qv": query_vector(persona, model),
        "has_loc": bool(locs), "locs": locs, "remote_ok": bool(persona.get("remote_ok")),
        "yrs": persona.get("career_years"),
        "nexcl": len(excl), "excl_like": [f"%{e}%" for e in excl],
        "overfetch": overfetch,
    }
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        cur.execute(SQL, params)
        rows = cur.fetchall()

    # 앱측 소프트 재랭킹 (retrieve.py 와 동일 공식)
    pskills = {s.lower() for s in (persona.get("skills") or [])}
    prefs = persona.get("company_size_pref") or []
    out = []
    for r in rows:
        overlap = len(pskills & {s.lower() for s in (r.get("skill_tags") or [])})
        overlap_n = overlap / max(1, len(pskills))
        size_b = 1.0 if size_ok(r.get("company_type"), prefs) else 0.0
        cos = float(r["score_cosine"])
        final = cos + 0.15 * overlap_n + 0.05 * size_b
        out.append({**r, "score_cosine": round(cos, 4),
                    "skill_overlap": overlap, "score_final": round(final, 4)})
    out.sort(key=lambda x: x["score_final"], reverse=True)
    return out[:topn]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument("--personas", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"))
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    ap.add_argument("--topn", type=int, default=20)
    ap.add_argument("--overfetch", type=int, default=200, help="KNN 1차 확보 수(부스트 재랭킹 여유분)")
    ap.add_argument("--out", default=None)
    a = ap.parse_args()
    if not a.dsn:
        sys.exit("DATABASE_URL(또는 --dsn) 필요")
    personas = {p["id"]: p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas[a.persona]

    conn = psycopg2.connect(a.dsn)
    model = TextEmbedding(model_name=MODEL)
    cands = retrieve_pg(persona, conn, model, a.topn, a.overfetch)
    conn.close()

    print(f"[{persona['label']}] 후보 {len(cands)}건 (topn={a.topn}, pgvector KNN)")
    for c in cands[:a.topn]:
        print(f"  {c['score_final']:.3f} (cos {c['score_cosine']:.3f}, skill×{c['skill_overlap']}) "
              f"{(c['position'] or '')[:34]} @ {c['company']} [{c.get('company_type')}/{c.get('employee_count')}명]")
    out = a.out or f"candidates_pg_{a.persona}.json"
    json.dump({"persona": persona, "candidates": cands}, open(out, "w"), ensure_ascii=False, indent=2)
    print(f"→ {out}")


if __name__ == "__main__":
    main()
