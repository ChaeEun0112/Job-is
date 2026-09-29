# Job.is 공고 DB — 백엔드 인계 패키지

IT/개발 채용공고 **1,000건** + 기업 **618곳**을 PostgreSQL로 바로 적재할 수 있는 세트.

```
database/
├── README.md            ← 이 문서 (적재 방법)
├── DATA_DICTIONARY.md   ← 테이블·컬럼·enum·갭 (먼저 읽기)
├── schema.sql           ← DDL (PostgreSQL 14+)
├── load.py              ← JSONL → Postgres 적재기 (idempotent)
├── requirements.txt     ← psycopg2-binary
├── .env.example         ← DATABASE_URL 예시
└── data/
    ├── companies.jsonl      (618줄)
    └── job_postings.jsonl   (1000줄)
```

## 빠른 시작

```sh
# 0) 의존성
pip install -r requirements.txt      # psycopg2-binary

# 1) DB 준비 (예: 로컬 도커)
docker run -d --name jobis-pg -e POSTGRES_PASSWORD=jobis -e POSTGRES_USER=jobis \
  -e POSTGRES_DB=jobis -p 127.0.0.1:5432:5432 postgres:16

export DATABASE_URL="postgresql://jobis:jobis@127.0.0.1:5432/jobis"
# set DATABASE_URL=postgresql://postgres:1234@localhost:5432/jobisbe

# 2) 스키마 생성 + 적재 (한 번에)
python load.py --dir data --schema schema.sql --init
#   → [load] companies 618행 / job_postings 1000행 (company_id 미결선 0건)

# (재적재만, 스키마 유지) : python load.py --dir data
```

- `--init` = 테이블 드롭 후 재생성 + 적재. 스키마를 이미 만들었으면 `--init` 없이 적재(upsert).
- 적재는 **idempotent** — `(source, external_id)` / `normalized_name` upsert라 반복 실행해도 중복 안 생김.

## 검증 (적재 후)

```sql
-- 결선 확인: 0 이어야 함
SELECT count(*) FROM job_postings WHERE company_id IS NULL;

-- 초개인화 샘플: 서울·경력 3년 이하·React 요구 공고 + 기업메타
SELECT j.position, c.name, c.employee_count, c.company_type, j.skill_tags
FROM job_postings j JOIN companies c ON c.id = j.company_id
WHERE j.location_city = '서울' AND j.career_min <= 3 AND 'React' = ANY(j.skill_tags)
LIMIT 10;
```

## 스냅샷 요약

| | |
|---|---|
| 공고 / 기업 | 1,000 / 618 |
| 직군 | 개발/IT 100% (MVP 한정) |
| JD·썸네일 | 100% · skill 95% · geo 99.7% |
| 기업 보강 | enriched 40% (나머지 원티드 업종 fallback) |
| 급여 | 없음(원티드 비공개) |

세부 컬럼 의미·enum·주의사항은 **`DATA_DICTIONARY.md`** 참고.

## ⚠️ 주의

- **`skills_inferred=true`** 공고(446건)의 skill은 NLP 텍스트 추론이라 저신뢰 — 정밀 하드필터엔 `false`(원티드 API 태그)만 신뢰 권장, 의미매칭엔 전량 사용 가능.
- **급여 컬럼 없음** — 원티드 비공개 정책.
