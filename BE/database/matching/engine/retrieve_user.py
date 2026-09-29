"""신호 통합 후보검색 (구현 순서 3단계 통합): DB 유저신호 → 후보군.

retrieve_pg 가 persona JSON 을 읽었다면, 이건 **DB 의 유저신호를 직접 읽어** 매칭한다:
  · 신호① user_preferences   → 하드필터(지역/경력/제외) + 선언 쿼리 임베딩
  · 신호② user_documents     → 이력서 임베딩(JD 와 대칭). user_skills.canonical_id → 공고 skill_tag_ids 와 동일축 교집합
  · 신호④ user_signal_state  → taste_vector (있으면 블렌드; step4)
user_vector = blend(선언, 이력서, taste) — 가용 신호에 따라 비중 동적(콜드→웜).

  export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis
  python retrieve_user.py --persona p2-senior-backend --topn 20
"""
from __future__ import annotations
import argparse, json, os, sys
import numpy as np
import psycopg2
from psycopg2.extras import RealDictCursor
from fastembed import TextEmbedding
from retrieve import MODEL, size_ok

# pgvector KNN 검색 기반 SQL로 변경
SQL = """
      SELECT jp.external_id, jp.position, c.name AS company,
             c.company_type, c.employee_count, c.industry,
             jp.location_city, jp.location_full, jp.is_remote,
             jp.career_min, jp.career_max, jp.skill_tags, jp.skill_tag_ids, jp.skills_inferred, jp.source_url,
          left(regexp_replace(coalesce(jp.requirements,''), E'\\n', ' ', 'g'), 280) AS req_snippet,
          left(regexp_replace(coalesce(jp.main_tasks,''),  E'\\n', ' ', 'g'), 280) AS tasks_snippet,
          1 - (jp.embedding <=> %(qv)s::vector) AS score_cosine,
          cardinality(ARRAY(SELECT unnest(jp.skill_tag_ids)
          INTERSECT SELECT unnest(%(uskills)s::int[]))) AS skill_overlap
      FROM job_postings jp
          LEFT JOIN companies c ON c.id = jp.company_id
      WHERE jp.embedding IS NOT NULL
        AND (NOT %(has_loc)s OR jp.location_city = ANY(%(locs)s) OR (%(remote_ok)s AND jp.is_remote))
        AND (%(yrs)s::int IS NULL OR jp.career_min IS NULL OR jp.career_min <= %(yrs)s)
        AND (%(nexcl)s = 0 OR NOT (
          lower(coalesce(jp.position,'') || ' ' || coalesce(c.name,'') || ' ' ||
          left(regexp_replace(coalesce(jp.requirements,''), E'\\n', ' ', 'g'), 280) || ' ' ||
          left(regexp_replace(coalesce(jp.main_tasks,''), E'\\n', ' ', 'g'), 280)) LIKE ANY(%(excl_like)s)))
      ORDER BY jp.embedding <=> %(qv)s::vector
          LIMIT %(overfetch)s
      """



def as_vec(pgtext):
    return np.array(json.loads(pgtext), dtype=np.float32) if pgtext else None


def vec_literal(row):
    return "[" + ",".join(f"{x:.6f}" for x in row) + "]"


def load_signals(cur, identifier):
    if str(identifier).isdigit():
        cur.execute("SELECT id FROM users WHERE id=%s", (int(identifier),))
    else:
        cur.execute("SELECT id FROM users WHERE ext_ref=%s", (identifier,))
    u = cur.fetchone()
    if not u:
        sys.exit(f"유저 없음: {identifier}")
    uid = u["id"]

    cur.execute("SELECT * FROM user_preferences WHERE user_id=%s", (uid,))
    pref = cur.fetchone() or {}

    # 1. user_documents 이력서 임베딩 조회 (선택적)
    resume_emb = None
    try:
        cur.execute("SAVEPOINT sp_doc")
        cur.execute(
            "SELECT embedding::text AS emb FROM user_documents "
            "WHERE user_id=%s AND doc_type='resume' AND is_active AND embedding IS NOT NULL LIMIT 1",
            (uid,)
        )
        doc = cur.fetchone()
        resume_emb = as_vec(doc["emb"]) if doc and doc.get("emb") else None
        cur.execute("RELEASE SAVEPOINT sp_doc")
    except Exception:
        cur.execute("ROLLBACK TO SAVEPOINT sp_doc")

    # 2. user_signal_state 취향 벡터 조회 (선택적)
    taste = None
    event_count = 0
    try:
        cur.execute("SAVEPOINT sp_taste")
        cur.execute(
            "SELECT taste_vector::text AS tv, event_count FROM user_signal_state WHERE user_id=%s",
            (uid,)
        )
        s = cur.fetchone()
        taste = as_vec(s["tv"]) if s and s.get("tv") else None
        event_count = (s or {}).get("event_count", 0)
        cur.execute("RELEASE SAVEPOINT sp_taste")
    except Exception:
        cur.execute("ROLLBACK TO SAVEPOINT sp_taste")

    # 3. user_skills 조회 (선택적)
    uskills = []
    try:
        cur.execute("SAVEPOINT sp_skills")
        cur.execute(
            "SELECT array_agg(DISTINCT canonical_id) AS ids FROM user_skills "
            "WHERE user_id=%s AND canonical_id IS NOT NULL",
            (uid,)
        )
        uskills = (cur.fetchone() or {}).get("ids") or []
        cur.execute("RELEASE SAVEPOINT sp_skills")
    except Exception:
        cur.execute("ROLLBACK TO SAVEPOINT sp_skills")

    return uid, pref, resume_emb, taste, event_count, uskills


def declared_text(pref):
    return " / ".join(filter(None, [
        " ".join(pref.get("interest_roles") or []),
        pref.get("preferred_free") or "",
        ]))


def blend(declared, resume, taste, event_count):
    """가용 신호로 user_vector 합성 (콜드→웜). 반환: (vector, weights_dict)."""
    w = {"declared": 1.0, "resume": 0.0, "taste": 0.0}
    if resume is not None:
        w = {"declared": 0.4, "resume": 0.6, "taste": 0.0}
    if taste is not None and event_count:                 # step4: 쓸수록 taste 비중↑
        boost = min(0.4, 0.05 * event_count)
        w = {"declared": w["declared"] * (1 - boost),
             "resume": w["resume"] * (1 - boost), "taste": boost}
    parts = [(declared, w["declared"]), (resume, w["resume"]), (taste, w["taste"])]
    acc = np.zeros_like(declared)
    for v, wt in parts:
        if v is not None and wt:
            acc = acc + wt * v
    acc /= (np.linalg.norm(acc) + 1e-9)
    return acc, w


def retrieve_user(ext_ref, conn, model, topn=20, overfetch=200):
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        uid, pref, resume_emb, taste, ecount, uskills = load_signals(cur, ext_ref)

        locs = list(pref.get("locations") or [])
        excl = [e.lower() for e in (pref.get("excludes") or [])]

        # 유저 혼합 벡터 및 weights 생성
        # embed() 는 fastembed>=0.3 에서 제너레이터를 돌려주므로 list() 로 풀고 첫 벡터를 꺼낸다.
        declared = declared_text(pref)
        declared_emb = (np.array(list(model.embed([declared])), dtype=np.float32)[0]
                        if declared else np.zeros(384, dtype=np.float32))
        user_vec, weights = blend(declared_emb, resume_emb, taste, ecount)

        # SQL 전달용 params (qv 포함)
        params = {
            "qv": vec_literal(user_vec),
            "uskills": uskills,
            "has_loc": bool(locs), "locs": locs, "remote_ok": bool(pref.get("remote_ok")),
            "yrs": pref.get("career_years"),
            "nexcl": len(excl), "excl_like": [f"%{e}%" for e in excl], "overfetch": overfetch,
        }
        cur.execute(SQL, params)
        rows = cur.fetchall()

    prefs = pref.get("company_size_pref") or []
    n_uskills = max(1, len(uskills))
    out = []
    for r in rows:
        overlap = r["skill_overlap"] or 0
        size_b = 1.0 if size_ok(r.get("company_type"), prefs) else 0.0
        cos = float(r["score_cosine"])
        final = cos + 0.15 * (overlap / n_uskills) + 0.05 * size_b
        row = {k: v for k, v in r.items() if k != "skill_tag_ids"}
        out.append({**row, "score_cosine": round(cos, 4),
                    "skill_overlap": overlap, "score_final": round(final, 4)})
    out.sort(key=lambda x: x["score_final"], reverse=True)

    # 실제 계산된 weights 반환
    return {"uid": uid, "weights": weights, "resume": resume_emb is not None,
            "taste": taste is not None, "n_uskills": len(uskills)}, out[:topn]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument("--personas", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"))
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    ap.add_argument("--topn", type=int, default=20)
    ap.add_argument("--overfetch", type=int, default=200)
    ap.add_argument("--out", default=None)
    a = ap.parse_args()
    if not a.dsn:
        sys.exit("DATABASE_URL(또는 --dsn) 필요")
    personas = {p["id"]: p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas[a.persona]

    conn = psycopg2.connect(a.dsn)
    model = TextEmbedding(model_name=MODEL)
    info, cands = retrieve_user(persona["id"], conn, model, a.topn, a.overfetch)
    conn.close()

    print(f"[{persona['label']}] user#{info['uid']} 후보 {len(cands)}건 | "
          f"신호: resume={info['resume']} taste={info['taste']} canonical_skills={info['n_uskills']} | "
          f"blend={info['weights']}")
    for c in cands[:a.topn]:
        print(f"  {c['score_final']:.3f} (cos {c['score_cosine']:.3f}, skill×{c['skill_overlap']}) "
              f"{(c['position'] or '')[:34]} @ {c['company']} [{c.get('company_type')}/{c.get('employee_count')}명]")
    out = a.out or f"candidates_user_{a.persona}.json"
    json.dump({"persona": persona, "signals": info, "candidates": cands},
              open(out, "w"), ensure_ascii=False, indent=2)
    print(f"→ {out}")


if __name__ == "__main__":
    main()

