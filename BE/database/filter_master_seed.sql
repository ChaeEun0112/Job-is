-- ============================================================
-- Job.is 기준 데이터(마스터) 시드 — regions / job_categories
--
-- 출처: planning/03-matching-contract.md 부록 A (단일 원본)
--   · A.1 관심 직무 — 대표 세부직군
--   · A.2 희망 지역 — location_city granularity(시·도 단위) 17개
--   screen-spec/60-profile.md PRO-01 필드 표가 위 부록을 선택지 원본으로 지정한다.
--
-- 선행 조건: regions / job_categories 테이블은 JPA(ddl-auto)가 생성한다.
--            앱을 한 번 띄워 테이블이 만들어진 뒤 실행할 것.
--            (database/schema.sql 은 크롤링 수집 스키마로 이 테이블들과 무관하다)
--
-- 실행: psql -h <host> -p <port> -U <user> -d <db> -f database/filter_master_seed.sql
--
-- 재실행 안전: 이름이 같은 행이 이미 있으면 건너뛴다(WHERE NOT EXISTS).
--              값을 고쳐 다시 넣으려면 해당 행을 지우고 실행할 것.
-- ============================================================

-- 이 파일은 UTF-8 이다. Windows cmd 의 psql 은 기본 클라이언트 인코딩이
-- UHC(CP949)라 한글이 깨지므로 세션 인코딩을 먼저 지정한다.
SET client_encoding TO 'UTF8';

BEGIN;

-- ------------------------------------------------------------
-- 1) regions — 시·도 17개 (부록 A.2)
--
-- parent_id 는 NULL 이다. Region 엔티티가 시/도-구/군 2단 계층을 지원하지만,
-- 부록 A.2 는 location_city 어휘(시·도 단위)를 기준으로 하므로 1단만 사용한다.
--
-- '원격'은 여기에 넣지 않는다. PRO-01 필드 표에서 지역 칩과 별개인
-- '원격 포함 토글'(user_preferences.remote_ok)로 정의돼 있다.
-- ------------------------------------------------------------
-- 기존 행이 있는데 시퀀스가 뒤처져 있으면 id 충돌(regions_pkey)이 난다.
-- 다음 발급값을 max(id)+1 로 맞춘다.
SELECT setval(
    pg_get_serial_sequence('regions', 'id'),
    COALESCE((SELECT MAX(id) FROM regions), 0) + 1,
    false
);

INSERT INTO regions (name, parent_id, sort_order, created_at, updated_at)
SELECT v.name, NULL, v.sort_order, localtimestamp, localtimestamp
FROM (VALUES
    ('서울',  1),
    ('경기',  2),
    ('인천',  3),
    ('부산',  4),
    ('대구',  5),
    ('광주',  6),
    ('대전',  7),
    ('울산',  8),
    ('세종',  9),
    ('강원', 10),
    ('충북', 11),
    ('충남', 12),
    ('전북', 13),
    ('전남', 14),
    ('경북', 15),
    ('경남', 16),
    ('제주', 17)
) AS v(name, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM regions r WHERE r.name = v.name
);

-- ------------------------------------------------------------
-- 2) job_categories — 대표 세부직군 13개 (부록 A.1)
--
-- name       = 세부직군(자동완성 후보, 사용자가 고르는 값)
-- group_name = 대분류(상위 직군)
--
-- 부록 A.1 의 '기타 = 자유 입력'은 마스터 행이 아니다.
-- 자동완성에 매칭되지 않는 입력을 텍스트 그대로 받는 예외 경로를 뜻한다.
-- ------------------------------------------------------------
-- regions 와 같은 이유로 시퀀스를 먼저 맞춘다.
SELECT setval(
    pg_get_serial_sequence('job_categories', 'id'),
    COALESCE((SELECT MAX(id) FROM job_categories), 0) + 1,
    false
);

INSERT INTO job_categories (name, group_name, sort_order, created_at, updated_at)
SELECT v.name, v.group_name, v.sort_order, localtimestamp, localtimestamp
FROM (VALUES
    ('백엔드 개발자',      '백엔드',        1),
    ('프론트엔드 개발자',  '프론트엔드',     2),
    ('풀스택 개발자',      '풀스택',        3),
    ('iOS 개발자',         '모바일',        4),
    ('Android 개발자',     '모바일',        5),
    ('데이터 엔지니어(DE)', '데이터',        6),
    ('데이터 분석가(DA)',   '데이터',        7),
    ('머신러닝 엔지니어(ML)','데이터',       8),
    ('DevOps 엔지니어',    'DevOps·인프라',  9),
    ('인프라 엔지니어',    'DevOps·인프라', 10),
    ('QA 엔지니어',        'QA',           11),
    ('보안 엔지니어',      '보안',          12),
    ('임베디드 SW 개발자', '임베디드',      13)
) AS v(name, group_name, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM job_categories c WHERE c.name = v.name
);

COMMIT;

-- ------------------------------------------------------------
-- 확인용 (실행 후 눈으로 검증)
-- ------------------------------------------------------------
-- SELECT count(*) AS regions FROM regions;              -- 기대: 17
-- SELECT count(*) AS job_categories FROM job_categories; -- 기대: 13
-- SELECT group_name, count(*) FROM job_categories GROUP BY 1 ORDER BY 1;

-- ------------------------------------------------------------
-- 후속 검증 (부록 A 단서 — 지금은 하지 않음)
--
-- 부록 A.1/A.2 는 위 목록이 '시안'이며 실 서비스 어휘는 공고 데이터로
-- 최종 검증하라고 단서를 달았다. 공고가 쌓인 뒤 아래를 돌려
-- 마스터 목록과 실제 공고 어휘의 간극을 확인할 것.
--
--   SELECT location_city, count(*) FROM job_postings GROUP BY 1 ORDER BY 2 DESC;
--   SELECT unnest(category_child) AS cat, count(*) FROM job_postings GROUP BY 1 ORDER BY 2 DESC;
--
-- 목록에 있으나 공고가 0건인 값은 사용자가 골라도 추천이 0건이 된다.
-- 비활성 처리할지 별도 표기할지는 부록 B 후속 항목이다.
-- ------------------------------------------------------------
