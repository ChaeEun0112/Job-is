-- Job.is 수집 스키마 (MVP)
-- 아키텍처: 원티드 = 공고 본체(base) / 잡코리아 = 기업 메타 보강(enrichment)
-- 근거: jobis-db/experiments/ 30건 실측 + design/EXTRACTION_GUIDE.md
-- PostgreSQL 14+ (pgvector는 추후 추천 단계에서 추가)

-- ============================================================
-- companies : 원티드로 식별 + 잡코리아 CORP_INFO로 메타 보강
-- ============================================================
CREATE TABLE companies (
  id                bigserial PRIMARY KEY,

  -- 식별 (출처: 원티드 initialData.company)
  name              text NOT NULL,                 -- company_name 원문
  normalized_name   text NOT NULL,                 -- 조인/매칭 키: (주)·공백 제거 + 소문자
  registration_number text,                        -- 사업자등록번호 (company.registration_number)
  wanted_company_id bigint,                         -- company.company_id

  -- 메타 (출처: 잡코리아 공고 RSC의 CORP_INFO 객체)
  jobkorea_gno_ref  bigint,                         -- 메타를 추출한 잡코리아 공고 Gno (출처추적)
  employee_count    integer,                        -- CORP_INFO.employeeCount (정수)
  company_type      text,                           -- CORP_INFO.companyTypeName (대기업/중견기업/중소기업)
  industry          text,                           -- CORP_INFO.industryName (없으면 원티드 industry_name)
  stock_status      text,                           -- CORP_INFO.stockStatusName (코스피/코스닥/-)
  hq_address        text,                           -- CORP_INFO.address
  homepage          text,
  description       text,                           -- 원티드 company_description

  -- enrichment 상태/감사
  --   enriched      : 잡코리아 회사명 일치, 메타(employee_count 등) 채움
  --   name_mismatch : 후보는 있었으나 이름 불일치 → 메타 미채움, rejected_name 만 기록
  --   not_found     : 잡코리아 검색결과 없음 → 메타 미채움
  --   ※ name_mismatch/not_found 행은 employee_count 등이 NULL (타사 값 저장 안 함 — 무결성)
  enrichment_status text NOT NULL DEFAULT 'pending'
    CHECK (enrichment_status IN ('pending','enriched','not_found','name_mismatch')),
  name_match        boolean,                        -- 원티드명 ↔ 잡코리아명 정규화 일치 여부
  rejected_name     text,                           -- name_mismatch 시 잡코리아가 준 엉뚱한 회사명(감사용)
  enriched_at       timestamptz,
  raw_jobkorea      jsonb,                          -- CORP_INFO 원본 보존

  created_at        timestamptz NOT NULL DEFAULT now(),
  updated_at        timestamptz NOT NULL DEFAULT now(),
  UNIQUE (normalized_name)
);

-- ============================================================
-- job_postings : 원티드가 본체 소스
-- ============================================================
CREATE TABLE job_postings (
  id                bigserial PRIMARY KEY,
  source            text NOT NULL DEFAULT 'wanted',
  external_id       bigint NOT NULL,                -- 원티드 wd id
  source_url        text NOT NULL,
  company_id        bigint REFERENCES companies(id),

  position          text NOT NULL,                  -- 직무명 (initialData.position)
  -- JD 의미단위 분리 필드 (원티드의 핵심 강점 — 그대로 컬럼화)
  intro             text,                           -- 팀/회사 소개
  main_tasks        text,                           -- 담당업무
  requirements      text,                           -- 자격요건
  preferred_points  text,                           -- 우대사항
  benefits          text,                           -- 복지/문화

  category_parent   text,                           -- category_tag.parent_tag (예: 개발)
  category_child    text[] DEFAULT '{}',            -- child_tags (예: {iOS 개발자})

  career_min        integer,                        -- career.annual_from  ※ 경력 '연수' (연봉 아님)
  career_max        integer,                        -- career.annual_to
  is_newbie         boolean,
  is_expert         boolean,
  employment_type   text,                           -- regular 등

  location_country  text,
  location_city     text,
  location_district text,
  location_full     text,
  geo_lat           double precision,               -- 상세 API geo_location (거리기반 초개인화)
  geo_lng           double precision,
  is_remote         boolean,

  due_time          timestamptz,                    -- null = 상시채용
  confirm_time      timestamptz,                    -- 게시 확정일
  status            text,                           -- active 등
  hire_rounds       text,                           -- 전형절차(텍스트)

  -- 스킬: 상세 API job.skill_tags (~30%, 개발직군 더 많음, 기업 임의태깅이라 노이즈 있음)
  --       빈 경우 requirements/preferred NLP 보강 (별도 단계)
  skill_tags        text[] DEFAULT '{}',            -- 스킬명 (예: {Python,React})
  skill_tag_ids     integer[] DEFAULT '{}',         -- 원티드 canonical id (동의어 정규화/매칭)
  skills_inferred   boolean NOT NULL DEFAULT false, -- true=NLP 텍스트 추론(저신뢰) / false=원티드 API 태그(정답)
  reward_total      text,                           -- 추천 보상금 (예: "100만원")

  -- 이미지: 상세 API. title_img/company_images = '회사 브랜딩 배너'(JD 정보 아님, 썸네일용).
  --         현재 설계(원티드=JD 텍스트, 잡코리아=기업메타 정형필드)에선 OCR 불필요.
  --         OCR 은 향후 잡코리아/사람인 'JD 본문'을 공고 소스로 승격할 때만 검토.
  thumbnail_url     text,                           -- 카드 썸네일 (title_img→company_images→logo 폴백)
  image_urls        text[] DEFAULT '{}',            -- 전체 이미지 URL

  raw               jsonb,                          -- initialData + 상세 API 원본
  collected_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (source, external_id)
);

CREATE INDEX idx_jp_company    ON job_postings(company_id);
CREATE INDEX idx_jp_category   ON job_postings(category_parent);
CREATE INDEX idx_jp_due        ON job_postings(due_time);
CREATE INDEX idx_jp_skills     ON job_postings USING gin(skill_tags);
CREATE INDEX idx_jp_skill_ids  ON job_postings USING gin(skill_tag_ids);
CREATE INDEX idx_jp_child      ON job_postings USING gin(category_child);
CREATE INDEX idx_co_regnum     ON companies(registration_number);
CREATE INDEX idx_co_enrich     ON companies(enrichment_status);

-- ============================================================
-- COMMENT (psql \d+ / 카탈로그에서 바로 보이는 컬럼 설명 — 함정 위주)
-- ============================================================
COMMENT ON COLUMN companies.normalized_name  IS '조인/매칭 키. job_postings 는 company_id(FK)로 연결됨';
COMMENT ON COLUMN job_postings.career_min    IS '경력 최소 "연수"(년). ※ 연봉 아님';
COMMENT ON COLUMN job_postings.career_max    IS '경력 최대 "연수"(년). ※ 연봉 아님';
COMMENT ON COLUMN job_postings.due_time      IS 'NULL = 상시채용';
COMMENT ON COLUMN job_postings.skills_inferred IS 'true=NLP 추론(저신뢰), false=원티드 API 태그. 하드필터는 false만 신뢰 권장';
COMMENT ON COLUMN job_postings.thumbnail_url IS '회사 브랜딩 배너(카드 썸네일용). JD 정보 아님';
COMMENT ON COLUMN job_postings.geo_lat       IS '근무지 위도(거리기반 매칭). NULL 소수 존재';
COMMENT ON TABLE  job_postings               IS '원티드 공고. 급여 컬럼 없음(비공개). JD는 5개 텍스트 컬럼(intro/main_tasks/requirements/preferred_points/benefits)';

-- 추후 추천 단계:
--   CREATE EXTENSION vector;
--   ALTER TABLE job_postings ADD COLUMN embedding vector(1536);
