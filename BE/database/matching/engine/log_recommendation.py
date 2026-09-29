"""추천 로깅 뼈대 (구현 순서 2단계): LLM 선별 결과 → L4/L5 테이블 적재.

개선 루프의 뼈대. '무엇을·왜 노출했는가'를 저장해야 피드백을 노출건과 연결할 수 있다.
  · users / user_preferences   ← 페르소나를 실제 유저 신호①로 적재
  · rec_bundles / rec_items     ← 오늘의 추천 + 이유·근거·약점·단계별 점수 (L4)
  · user_events                 ← impression 자동 로깅 (+ --simulate 로 like/dislike 예시)

입력: recommend_pg.py 산출물(reco_pg_<persona>.json) + 후보(candidates_pg_<persona>.json, 단계별 점수).

  export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis
  python log_recommendation.py --persona p2-senior-backend --simulate
"""
from __future__ import annotations
import argparse, json, os, sys
import psycopg2
from psycopg2.extras import Json, RealDictCursor


def career_stage(years):
    if years is None or years < 1:
        return "newbie"
    return "1_3" if years <= 3 else "3plus"


def upsert_user(cur, persona):
    cur.execute(
        "INSERT INTO users (ext_ref) VALUES (%s) "
        "ON CONFLICT (ext_ref) DO UPDATE SET ext_ref = EXCLUDED.ext_ref RETURNING id",
        (persona["id"],))
    return cur.fetchone()["id"]


def upsert_preferences(cur, uid, persona):
    cur.execute(
        """INSERT INTO user_preferences
           (user_id, interest_roles, career_stage, career_years, locations,
            remote_ok, company_size_pref, preferred_free, excludes, updated_at)
           VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s, now())
               ON CONFLICT (user_id) DO UPDATE SET
            interest_roles=EXCLUDED.interest_roles, career_stage=EXCLUDED.career_stage,
                                            career_years=EXCLUDED.career_years, locations=EXCLUDED.locations,
                                            remote_ok=EXCLUDED.remote_ok, company_size_pref=EXCLUDED.company_size_pref,
                                            preferred_free=EXCLUDED.preferred_free, excludes=EXCLUDED.excludes, updated_at=now()""",
        (uid, persona.get("interest_roles") or [], career_stage(persona.get("career_years")),
         persona.get("career_years"), persona.get("locations") or [],
         bool(persona.get("remote_ok")), persona.get("company_size_pref") or [],
         persona.get("free_text"), persona.get("exclude") or []))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument("--personas", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"))
    ap.add_argument("--reco", default=None, help="기본 reco_pg_<persona>.json")
    ap.add_argument("--candidates", default=None, help="기본 candidates_pg_<persona>.json(단계별 점수)")
    ap.add_argument("--bundle-date", default=None, help="기본 CURRENT_DATE")
    ap.add_argument("--model", default="agent-proxy(claude)")
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    ap.add_argument("--simulate", action="store_true", help="like/dislike 이벤트 예시 삽입(루프 데모)")
    a = ap.parse_args()
    if not a.dsn:
        sys.exit("DATABASE_URL(또는 --dsn) 필요")

    personas = {p["id"]: p for p in json.load(open(a.personas, encoding="utf-8"))}
    persona = personas[a.persona]
    reco = json.load(open(a.reco or f"reco_pg_{a.persona}.json", encoding="utf-8"))
    cand_path = a.candidates or f"candidates_pg_{a.persona}.json"
    scores = {}
    if os.path.exists(cand_path):
        for c in json.load(open(cand_path, encoding="utf-8"))["candidates"]:
            scores[c["external_id"]] = {"cosine": c.get("score_cosine"),
                                        "skill_overlap": c.get("skill_overlap"),
                                        "final": c.get("score_final")}

    conn = psycopg2.connect(a.dsn)
    conn.autocommit = False
    cur = conn.cursor(cursor_factory=RealDictCursor)

    # 신호① : 유저 + 선언 선호
    uid = upsert_user(cur, persona)
    upsert_preferences(cur, uid, persona)

    # L4 : 번들 (유저×일자 유니크 → 재실행 시 교체)
    bdate = a.bundle_date
    cur.execute(
        "INSERT INTO rec_bundles (user_id, bundle_date) VALUES (%s, COALESCE(%s::date, CURRENT_DATE)) "
        "ON CONFLICT (user_id, bundle_date) DO UPDATE SET user_id=EXCLUDED.user_id RETURNING id",
        (uid, bdate))
    bundle_id = cur.fetchone()["id"]

    # 멱등 재적재: 기존 아이템을 교체하되 사용자 피드백은 보존한다.
    # user_events.rec_item_id 에는 FK 가 없어 rec_items 를 지워도 DB 가 막아주지 않는다.
    # 그대로 두면 삭제된 행을 가리키는 고아 참조가 남으므로,
    # (user_id, posting_id) 를 기준으로 새 아이템에 다시 연결한다.
    cur.execute(
        """SELECT ev.id, ri.posting_id
           FROM user_events ev
                    JOIN rec_items ri ON ri.id = ev.rec_item_id
           WHERE ri.bundle_id=%s AND ev.event_type <> 'impression'""",
        (bundle_id,))
    carry = {}
    for row in cur.fetchall():
        carry.setdefault(row["posting_id"], []).append(row["id"])

    # 노출(impression)은 이번 적재에서 다시 생성되므로 정리한다.
    cur.execute(
        """DELETE FROM user_events
           WHERE event_type = 'impression'
             AND rec_item_id IN (SELECT id FROM rec_items WHERE bundle_id=%s)""",
        (bundle_id,)
    )
    cur.execute("DELETE FROM rec_items WHERE bundle_id=%s", (bundle_id,))

    # external_id → job_postings.id
    recs = reco.get("recommendations", [])
    ext_ids = [r["external_id"] for r in recs]
    cur.execute("SELECT external_id, id FROM job_postings WHERE source='wanted' AND external_id = ANY(%s)",
                (ext_ids,))
    pid = {row["external_id"]: row["id"] for row in cur.fetchall()}

    n_items = 0
    n_imp = 0
    n_relinked = 0
    for r in recs:
        ext = r["external_id"]
        posting_id = pid.get(ext)
        if posting_id is None:
            print(f"  ! external_id {ext} 미결선 — 스킵", file=sys.stderr)
            continue
        sc = scores.get(ext, {})
        fit_score = int(round((sc.get("final") or 0) * 100)) or None
        cur.execute(
            """INSERT INTO rec_items
               (bundle_id, posting_id, rank, fit_score, reason, fit_points, caution, stage_scores, model)
               VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s) RETURNING id""",
            (bundle_id, posting_id, r.get("rank"), fit_score, r.get("reason"),
             Json(r.get("fit_points") or []), r.get("caution") or "", Json(sc), a.model))
        rec_item_id = cur.fetchone()["id"]
        n_items += 1
        # 같은 공고에 남아 있던 이전 피드백을 새 아이템으로 재연결(보존)
        carried = carry.pop(posting_id, None)
        if carried:
            cur.execute("UPDATE user_events SET rec_item_id=%s WHERE id = ANY(%s)",
                        (rec_item_id, carried))
            n_relinked += len(carried)
        # L5 : 노출(impression) 자동 로깅
        cur.execute(
            "INSERT INTO user_events (user_id, posting_id, rec_item_id, event_type) "
            "VALUES (%s,%s,%s,'impression')", (uid, posting_id, rec_item_id))
        n_imp += 1

    # 이번 번들에서 빠진 공고의 피드백은 연결할 새 아이템이 없다.
    # 삭제된 행을 계속 가리키지 않도록 참조만 끊고 이벤트 자체는 남긴다((user_id, posting_id) 로 추적 가능).
    orphaned = [eid for ids in carry.values() for eid in ids]
    if orphaned:
        cur.execute("UPDATE user_events SET rec_item_id = NULL WHERE id = ANY(%s)", (orphaned,))

    # 루프 데모용 피드백(선택) : 1위 like, 마지막 dislike+사유
    if a.simulate and n_items >= 2:
        cur.execute("SELECT id, posting_id, rank FROM rec_items WHERE bundle_id=%s ORDER BY rank", (bundle_id,))
        items = cur.fetchall()
        top, last = items[0], items[-1]
        cur.execute("INSERT INTO user_events (user_id, posting_id, rec_item_id, event_type) "
                    "VALUES (%s,%s,%s,'like')", (uid, top["posting_id"], top["id"]))
        cur.execute("INSERT INTO user_events (user_id, posting_id, rec_item_id, event_type, reason_code, comment) "
                    "VALUES (%s,%s,%s,'dislike','기업규모','더 큰 조직을 원해요')",
                    (uid, last["posting_id"], last["id"]))

    conn.commit()

    # 검증 : 번들 재구성 + 이벤트 집계
    cur.execute(
        """SELECT ri.rank, ri.fit_score, jp.position, c.name AS company, ri.reason,
                  ri.stage_scores,
                  count(ev.*) FILTER (WHERE ev.event_type='impression') AS impressions,
               count(ev.*) FILTER (WHERE ev.event_type='like')       AS likes,
               count(ev.*) FILTER (WHERE ev.event_type='dislike')    AS dislikes
           FROM rec_items ri
                    JOIN job_postings jp ON jp.id = ri.posting_id
                    LEFT JOIN companies c ON c.id = jp.company_id
                    LEFT JOIN user_events ev ON ev.rec_item_id = ri.id
           WHERE ri.bundle_id=%s
           GROUP BY ri.id, jp.position, c.name ORDER BY ri.rank""", (bundle_id,))
    print(f"[log] user#{uid} bundle#{bundle_id}: rec_items {n_items}, impressions {n_imp}"
          + (f", 피드백 재연결 {n_relinked}" if n_relinked else "")
          + (f", 참조해제 {len(orphaned)}" if orphaned else "")
          + (" (+simulate like/dislike)" if a.simulate else ""))
    for row in cur.fetchall():
        ss = row["stage_scores"] or {}
        print(f"  #{row['rank']} [{row['fit_score']}] {(row['position'] or '')[:32]} @ {row['company']}"
              f"  imp{row['impressions']}/like{row['likes']}/dis{row['dislikes']}"
              f"  (cos {ss.get('cosine')}, skill×{ss.get('skill_overlap')})")
        print(f"      → {row['reason']}")
    cur.close(); conn.close()


if __name__ == "__main__":
    main()
