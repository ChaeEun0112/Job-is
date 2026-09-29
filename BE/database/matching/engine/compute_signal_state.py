"""피드백 → 파생상태 재계산 (구현 순서 4단계): user_events → user_signal_state.

개선 루프의 심장. 노출·행동을 취향 신호로 응축한다(배치/준실시간 재계산 대상):
  · taste_vector : 좋아요/저장/지원한 공고 임베딩의 가중평균 (retrieve_user 블렌드에 반영)
  · pref_memory  : 싫어요 사유·코멘트 요약 (선별기 프롬프트 컨텍스트) — 운영은 LLM 요약, 여기선 규칙기반
  · event_count  : 참여도(콜드↔웜 전환 판단) → blend() 가 taste 비중 상향

retrieve_user 는 taste_vector 가 있으면 자동으로 블렌드하므로, 이 스크립트 실행 = 다음 추천이 달라짐.

  export DATABASE_URL=postgresql://jobis:jobis@127.0.0.1:5433/jobis
  python compute_signal_state.py --persona p2-senior-backend
"""
from __future__ import annotations
import argparse, json, os, sys
import numpy as np
import psycopg2
from psycopg2.extras import Json, RealDictCursor

POS_WEIGHT = {"like": 1.0, "save": 0.8, "apply_intent": 1.2, "apply": 1.5}
FEEDBACK = ("like", "dislike", "save", "skip", "apply_intent", "apply")


def as_vec(pgtext):
    return np.array(json.loads(pgtext), dtype=np.float32) if pgtext else None


def vec_literal(row):
    return "[" + ",".join(f"{x:.6f}" for x in row) + "]"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--persona", required=True)
    ap.add_argument("--personas", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "fixtures", "personas.json"))
    ap.add_argument("--dsn", default=os.environ.get("DATABASE_URL"))
    a = ap.parse_args()
    if not a.dsn:
        sys.exit("DATABASE_URL(또는 --dsn) 필요")
    persona_id = {p["id"]: p for p in json.load(open(a.personas, encoding="utf-8"))}[a.persona]["id"]

    conn = psycopg2.connect(a.dsn)
    conn.autocommit = False
    cur = conn.cursor(cursor_factory=RealDictCursor)
    cur.execute("SELECT id FROM users WHERE ext_ref=%s", (persona_id,))
    u = cur.fetchone()
    if not u:
        sys.exit(f"유저 없음: {persona_id}")
    uid = u["id"]

    # taste_vector : 긍정 이벤트 공고 임베딩 가중평균
    cur.execute(f"""
        SELECT jp.embedding::text AS emb, ev.event_type
        FROM user_events ev JOIN job_postings jp ON jp.id = ev.posting_id
        WHERE ev.user_id=%s AND ev.event_type IN %s AND jp.embedding IS NOT NULL""",
                (uid, tuple(POS_WEIGHT)))
    acc, wsum, n_pos = None, 0.0, 0
    for r in cur.fetchall():
        v = as_vec(r["emb"]); w = POS_WEIGHT[r["event_type"]]
        acc = (w * v) if acc is None else (acc + w * v)
        wsum += w; n_pos += 1
    taste = None
    if acc is not None:
        acc /= (np.linalg.norm(acc) + 1e-9)
        taste = vec_literal(acc)

    # pref_memory : 규칙기반 요약(운영은 LLM). 좋아요 패턴 + 싫어요 사유
    cur.execute("""
                SELECT c.company_type, jp.skill_tags
                FROM user_events ev JOIN job_postings jp ON jp.id=ev.posting_id
                                    LEFT JOIN companies c ON c.id=jp.company_id
                WHERE ev.user_id=%s AND ev.event_type IN ('like','save','apply','apply_intent')""", (uid,))
    liked = cur.fetchall()
    from collections import Counter
    like_types = Counter(x["company_type"] for x in liked if x["company_type"])
    like_skills = Counter(s for x in liked for s in (x["skill_tags"] or []))
    cur.execute("""
                SELECT reason_code, comment FROM user_events
                WHERE user_id=%s AND event_type='dislike'""", (uid,))
    dis = cur.fetchall()
    dis_reasons = Counter(x["reason_code"] for x in dis if x["reason_code"])
    pref_memory = {
        "likes": ([f"{t} 선호({c})" for t, c in like_types.most_common(3)]
                  + [f"{s} 관심({c})" for s, c in like_skills.most_common(3)]),
        "dislikes": ([f"{r} 사유 회피({c})" for r, c in dis_reasons.most_common()]
                     + [x["comment"] for x in dis if x["comment"]]),
    }

    # event_count : 참여도(노출 제외 실제 행동)
    cur.execute("SELECT count(*) AS c FROM user_events WHERE user_id=%s AND event_type IN %s",
                (uid, FEEDBACK))
    event_count = cur.fetchone()["c"]

    cur.execute("""
                INSERT INTO user_signal_state (user_id, taste_vector, pref_memory, event_count, updated_at)
                VALUES (%s, %s::vector, %s, %s, now())
                    ON CONFLICT (user_id) DO UPDATE SET
                    taste_vector=EXCLUDED.taste_vector, pref_memory=EXCLUDED.pref_memory,
                                                 event_count=EXCLUDED.event_count, updated_at=now()""",
                (uid, taste, Json(pref_memory), event_count))
    conn.commit()

    print(f"[state] user#{uid}: taste_vector={'set' if taste else 'none'}(긍정 {n_pos}건), "
          f"event_count={event_count}")
    print(f"  pref_memory.likes    = {pref_memory['likes']}")
    print(f"  pref_memory.dislikes = {pref_memory['dislikes']}")
    cur.close(); conn.close()


if __name__ == "__main__":
    main()
