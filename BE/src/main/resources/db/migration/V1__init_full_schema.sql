-- V1: Job.is 앱 전체 스키마 초기 생성
-- 모든 구문이 IF NOT EXISTS / ADD COLUMN IF NOT EXISTS를 사용하므로
-- 기존 DB(schema.sql로 수동 생성된 companies/job_postings가 있는 경우)에서도 안전하게 실행된다.

-- ============================================================
-- companies (Python 파이프라인 schema.sql로 이미 존재할 수 있음)
-- ============================================================
CREATE TABLE IF NOT EXISTS companies (
    id                  bigserial PRIMARY KEY,
    name                varchar(200),
    normalized_name     varchar(200),
    registration_number varchar(50),
    wanted_company_id   bigint,
    jobkorea_gno_ref    bigint,
    employee_count      integer,
    company_type        varchar(50),
    industry            varchar(100),
    stock_status        varchar(50),
    hq_address          varchar(500),
    homepage            varchar(500),
    description         text,
    enrichment_status   varchar(50) NOT NULL DEFAULT 'pending',
    name_match          boolean,
    rejected_name       varchar(200),
    enriched_at         timestamptz,
    raw_jobkorea        jsonb,
    source              varchar(50),
    logo_url            varchar(500),
    created_at          timestamp NOT NULL DEFAULT now(),
    updated_at          timestamp NOT NULL DEFAULT now(),
    UNIQUE (normalized_name)
);

-- 구 schema.sql에 없던 컬럼 보강 (기존 테이블에 대해서만 실질적으로 실행됨)
ALTER TABLE companies ADD COLUMN IF NOT EXISTS source   varchar(50);
ALTER TABLE companies ADD COLUMN IF NOT EXISTS logo_url varchar(500);

-- ============================================================
-- job_postings (Python 파이프라인 write target; 앱은 읽기 전용)
-- ============================================================
CREATE TABLE IF NOT EXISTS job_postings (
    id                bigserial PRIMARY KEY,
    source            varchar(50) NOT NULL DEFAULT 'wanted',
    external_id       bigint NOT NULL,
    source_url        text,
    company_id        bigint REFERENCES companies(id),
    position          text,
    intro             text,
    main_tasks        text,
    requirements      text,
    preferred_points  text,
    benefits          text,
    category_parent   varchar(50),
    category_child    text[],
    career_min        integer,
    career_max        integer,
    is_newbie         boolean,
    is_expert         boolean,
    employment_type   varchar(50),
    location_country  varchar(100),
    location_city     varchar(100),
    location_district varchar(100),
    location_full     text,
    geo_lat           double precision,
    geo_lng           double precision,
    is_remote         boolean,
    due_time          timestamptz,
    confirm_time      timestamptz,
    status            varchar(20),
    hire_rounds       text,
    skill_tags        text[],
    skill_tag_ids     integer[],
    skills_inferred   boolean,
    thumbnail_url     text,
    image_urls        text[],
    reward_total      varchar(100),
    raw               jsonb,
    collected_at      timestamptz,
    UNIQUE (source, external_id)
);

-- ============================================================
-- job_categories (직무/직군 마스터)
-- ============================================================
CREATE TABLE IF NOT EXISTS job_categories (
    id          bigserial PRIMARY KEY,
    name        varchar(100) NOT NULL,
    group_name  varchar(100),
    sort_order  integer,
    created_at  timestamp NOT NULL DEFAULT now(),
    updated_at  timestamp NOT NULL DEFAULT now()
);

-- ============================================================
-- regions (지역 마스터, 자기참조)
-- ============================================================
CREATE TABLE IF NOT EXISTS regions (
    id          bigserial PRIMARY KEY,
    name        varchar(100) NOT NULL,
    parent_id   bigint REFERENCES regions(id),
    sort_order  integer,
    created_at  timestamp NOT NULL DEFAULT now(),
    updated_at  timestamp NOT NULL DEFAULT now()
);

-- ============================================================
-- tech_stacks (기술스택 마스터)
-- ============================================================
CREATE TABLE IF NOT EXISTS tech_stacks (
    id              bigserial PRIMARY KEY,
    name            varchar(100) NOT NULL,
    normalized_name varchar(100) NOT NULL,
    created_at      timestamp NOT NULL DEFAULT now(),
    updated_at      timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_tech_stacks_name            UNIQUE (name),
    CONSTRAINT uk_tech_stacks_normalized_name UNIQUE (normalized_name)
);

-- ============================================================
-- users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id           bigserial PRIMARY KEY,
    social_id    varchar(255) NOT NULL,
    social_type  varchar(20)  NOT NULL,
    email        varchar(255) NOT NULL,
    status       varchar(20)  NOT NULL,
    role         varchar(20)  NOT NULL,
    withdrawn_at timestamp,
    created_at   timestamp NOT NULL DEFAULT now(),
    updated_at   timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_email  UNIQUE (email),
    CONSTRAINT uk_users_social UNIQUE (social_id, social_type)
);

-- ============================================================
-- jobs (앱 서빙용; job_postings를 정규화·변환한 테이블)
-- ============================================================
CREATE TABLE IF NOT EXISTS jobs (
    id                bigserial PRIMARY KEY,
    company_id        bigint REFERENCES companies(id),
    job_category_id   bigint REFERENCES job_categories(id),
    region_id         bigint REFERENCES regions(id),
    title             text NOT NULL,
    career_level      varchar(30),
    employment_type   varchar(30),
    remote_available  boolean,
    salary_disclosed  boolean,
    source            varchar(50),
    external_id       bigint,
    source_url        text,
    posted_at         timestamptz,
    deadline_at       timestamptz,
    status            varchar(20) NOT NULL,
    editor_note       varchar(1000),
    location_full     text,
    intro             text,
    main_tasks        text,
    requirements      text,
    preferred_points  text,
    benefits          text,
    career_min        integer,
    career_max        integer,
    reward_total      text,
    thumbnail_url     text,
    skills            text,
    categories        text,
    skill_tags        text[],
    skills_inferred   boolean,
    embedding         text,
    location_city     varchar(100),
    location_district varchar(100),
    is_newbie         boolean,
    category_child    text[],
    created_at        timestamp NOT NULL DEFAULT now(),
    updated_at        timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_jobs_source_external_id UNIQUE (source, external_id)
);

CREATE INDEX IF NOT EXISTS idx_jobs_company    ON jobs(company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_category   ON jobs(job_category_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status     ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_deadline   ON jobs(deadline_at);
CREATE INDEX IF NOT EXISTS idx_jobs_skill_tags ON jobs USING gin(skill_tags);

-- ============================================================
-- saved_jobs
-- ============================================================
CREATE TABLE IF NOT EXISTS saved_jobs (
    id                   bigserial PRIMARY KEY,
    user_id              bigint NOT NULL REFERENCES users(id),
    job_id               bigint NOT NULL REFERENCES jobs(id),
    saved_at             timestamptz NOT NULL,
    self_reported_status varchar(20),
    created_at           timestamp NOT NULL DEFAULT now(),
    updated_at           timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_saved_jobs_user_job UNIQUE (user_id, job_id)
);

-- ============================================================
-- user_profiles
-- ============================================================
CREATE TABLE IF NOT EXISTS user_profiles (
    id                      bigserial PRIMARY KEY,
    user_id                 bigint NOT NULL REFERENCES users(id),
    career_level            varchar(20),
    onboarding_step         varchar(20),
    preference_note         varchar(500),
    exclude_keywords        varchar(500),
    tech_stack              varchar(500),
    remote_ok               boolean NOT NULL DEFAULT false,
    personality_tags        varchar(500),
    is_job_test_completed   boolean NOT NULL DEFAULT false,
    job_test_completed_at   timestamp,
    onboarding_completed    boolean NOT NULL DEFAULT false,
    onboarding_completed_at timestamp,
    created_at              timestamp NOT NULL DEFAULT now(),
    updated_at              timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_profiles_user UNIQUE (user_id)
);

-- ============================================================
-- user_consents
-- ============================================================
CREATE TABLE IF NOT EXISTS user_consents (
    id                 bigserial PRIMARY KEY,
    user_id            bigint NOT NULL REFERENCES users(id),
    terms_agreed       boolean NOT NULL,
    privacy_agreed     boolean NOT NULL,
    age_over_14_agreed boolean NOT NULL,
    marketing_agreed   boolean NOT NULL,
    agreed_at          timestamp NOT NULL,
    created_at         timestamp NOT NULL DEFAULT now(),
    updated_at         timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_consents_user UNIQUE (user_id)
);

-- ============================================================
-- user_tech_stacks
-- ============================================================
CREATE TABLE IF NOT EXISTS user_tech_stacks (
    id            bigserial PRIMARY KEY,
    user_id       bigint NOT NULL REFERENCES users(id),
    tech_stack_id bigint NOT NULL REFERENCES tech_stacks(id),
    created_at    timestamp NOT NULL DEFAULT now(),
    updated_at    timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_tech_stacks_user_stack UNIQUE (user_id, tech_stack_id)
);

-- ============================================================
-- user_job_categories
-- ============================================================
CREATE TABLE IF NOT EXISTS user_job_categories (
    id              bigserial PRIMARY KEY,
    user_id         bigint NOT NULL REFERENCES users(id),
    job_category_id bigint NOT NULL REFERENCES job_categories(id),
    is_primary      boolean NOT NULL,
    created_at      timestamp NOT NULL DEFAULT now(),
    updated_at      timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_job_categories_user_category UNIQUE (user_id, job_category_id)
);

-- ============================================================
-- user_regions
-- ============================================================
CREATE TABLE IF NOT EXISTS user_regions (
    id          bigserial PRIMARY KEY,
    user_id     bigint NOT NULL REFERENCES users(id),
    region_id   bigint NOT NULL REFERENCES regions(id),
    created_at  timestamp NOT NULL DEFAULT now(),
    updated_at  timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_user_regions_user_region UNIQUE (user_id, region_id)
);

-- ============================================================
-- user_withdrawals
-- ============================================================
CREATE TABLE IF NOT EXISTS user_withdrawals (
    id                    bigserial PRIMARY KEY,
    user_id               bigint NOT NULL REFERENCES users(id),
    reason_code           varchar(30),
    reason_detail         varchar(500),
    requested_at          timestamp NOT NULL,
    scheduled_deletion_at timestamp,
    restored_at           timestamp,
    status                varchar(20) NOT NULL,
    created_at            timestamp NOT NULL DEFAULT now(),
    updated_at            timestamp NOT NULL DEFAULT now()
);

-- ============================================================
-- resumes
-- ============================================================
CREATE TABLE IF NOT EXISTS resumes (
    id          bigserial PRIMARY KEY,
    user_id     bigint NOT NULL REFERENCES users(id),
    category    varchar(20)  NOT NULL,
    file_name   varchar(255) NOT NULL,
    file_type   varchar(10)  NOT NULL,
    s3_key      varchar(500) NOT NULL,
    uploaded_at timestamp NOT NULL,
    deleted_at  timestamp,
    created_at  timestamp NOT NULL DEFAULT now(),
    updated_at  timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_resumes_user_category UNIQUE (user_id, category)
);

-- ============================================================
-- decks
-- ============================================================
CREATE TABLE IF NOT EXISTS decks (
    id              bigserial PRIMARY KEY,
    user_id         bigint NOT NULL REFERENCES users(id),
    deck_date       date NOT NULL,
    empty_reason    varchar(30),
    first_opened_at timestamptz,
    completed_at    timestamptz,
    created_at      timestamp NOT NULL DEFAULT now(),
    updated_at      timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_decks_user_date UNIQUE (user_id, deck_date)
);

-- ============================================================
-- cards
-- ============================================================
CREATE TABLE IF NOT EXISTS cards (
    id               bigserial PRIMARY KEY,
    deck_id          bigint NOT NULL REFERENCES decks(id),
    job_id           bigint REFERENCES jobs(id),
    position         integer NOT NULL,
    fit_score        numeric(5, 2),
    reason           varchar(300),
    summary          varchar(500),
    status           varchar(20) NOT NULL,
    reason_submitted boolean NOT NULL DEFAULT false,
    version          bigint,
    created_at       timestamp NOT NULL DEFAULT now(),
    updated_at       timestamp NOT NULL DEFAULT now()
);

-- ============================================================
-- user_actions
-- ============================================================
CREATE TABLE IF NOT EXISTS user_actions (
    id            bigserial PRIMARY KEY,
    user_id       bigint NOT NULL REFERENCES users(id),
    job_id        bigint NOT NULL REFERENCES jobs(id),
    action_type   varchar(30) NOT NULL,
    reason_code   varchar(30),
    comment       varchar(200),
    source_screen varchar(20),
    created_at    timestamp NOT NULL DEFAULT now(),
    updated_at    timestamp NOT NULL DEFAULT now()
);

-- ============================================================
-- notification_settings
-- ============================================================
CREATE TABLE IF NOT EXISTS notification_settings (
    id                   bigserial PRIMARY KEY,
    user_id              bigint NOT NULL REFERENCES users(id),
    email_subscribed     boolean NOT NULL DEFAULT true,
    marketing_subscribed boolean NOT NULL DEFAULT false,
    send_slot            varchar(10) NOT NULL,
    snooze_until         date,
    snooze_indefinite    boolean NOT NULL DEFAULT false,
    email_verified       boolean NOT NULL DEFAULT false,
    unsubscribe_token    varchar(64) NOT NULL,
    created_at           timestamp NOT NULL DEFAULT now(),
    updated_at           timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_notification_settings_user             UNIQUE (user_id),
    CONSTRAINT uk_notification_settings_unsubscribe_token UNIQUE (unsubscribe_token)
);

-- ============================================================
-- unsubscribe_feedbacks
-- ============================================================
CREATE TABLE IF NOT EXISTS unsubscribe_feedbacks (
    id          bigserial PRIMARY KEY,
    user_id     bigint NOT NULL REFERENCES users(id),
    reason      varchar(30) NOT NULL,
    comment     varchar(200),
    created_at  timestamp NOT NULL DEFAULT now(),
    updated_at  timestamp NOT NULL DEFAULT now()
);

-- ============================================================
-- personality_tests
-- ============================================================
CREATE TABLE IF NOT EXISTS personality_tests (
    id           bigserial PRIMARY KEY,
    user_id      bigint NOT NULL REFERENCES users(id),
    source       varchar(20) NOT NULL,
    is_completed boolean NOT NULL DEFAULT false,
    result_tags  varchar(500),
    result_type  varchar(2),
    started_at   timestamptz NOT NULL,
    completed_at timestamptz,
    created_at   timestamp NOT NULL DEFAULT now(),
    updated_at   timestamp NOT NULL DEFAULT now()
);

-- ============================================================
-- personality_test_answers
-- ============================================================
CREATE TABLE IF NOT EXISTS personality_test_answers (
    id           bigserial PRIMARY KEY,
    test_id      bigint NOT NULL REFERENCES personality_tests(id),
    question_no  integer NOT NULL,
    choice_value varchar(50) NOT NULL,
    answered_at  timestamp NOT NULL,
    created_at   timestamp NOT NULL DEFAULT now(),
    updated_at   timestamp NOT NULL DEFAULT now(),
    CONSTRAINT uk_personality_test_answers_test_question UNIQUE (test_id, question_no)
);
