
import os
import json
import psycopg2

def ingest_disposition(user_id: int, axes: dict, tags: list):
    db_url = os.environ.get("DATABASE_URL", "postgresql://jobis:jobis@127.0.0.1:5433/jobis")
    conn = psycopg2.connect(db_url)
    cur = conn.cursor()

    cur.execute("""
                INSERT INTO user_disposition (user_id, test_version, axes, tags, completed_at)
                VALUES (%s, %s, %s, %s, NOW())
                    ON CONFLICT (user_id) DO UPDATE
                                                 SET axes = EXCLUDED.axes, tags = EXCLUDED.tags, updated_at = NOW()
                """, (user_id, "v1.0", json.dumps(axes), tags))

    conn.commit()
    cur.close()
    conn.close()
    print(f"User {user_id} disposition ingested.")

if __name__ == "__main__":
    sample_axes = {
        "growth_vs_stability": 0.8,
        "culture_vs_compensation": 0.5,
        "autonomy_vs_structure": 0.7,
        "wlb_vs_intensity": 0.2,
        "individual_vs_team": 0.1,
        "tech_vs_business": 0.6
    }
    ingest_disposition(user_id=2, axes=sample_axes, tags=["성장지향", "자율선호"])
