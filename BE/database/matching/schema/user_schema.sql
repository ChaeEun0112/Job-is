-- Job.is 유저 신호 스키마 (L3) + 추천 로깅(L4) + 행동 이벤트(L5)
-- 설계 원칙: 초개인화 = 4가지 신호의 결합
--   ① 선언적 선호(declared)  ② 역량/증거(resume)  ③ 잠재 성향(test)  ④ 행동 피드백(behavioral)
-- 매칭 연결 방식은 SIGNAL_DESIGN.md 참조. job_postings/companies 는 기존 schema.sql 전제.
-- PostgreSQL 14+ / pgvector.

CREATE EXTENSION IF NOT EXISTS vector;

-- 공고 임베딩(matching → 프로덕션). 유저 벡터와 대칭 매칭.
ALTER TABLE job_postings ADD COLUMN IF NOT EXISTS embedding vector(384);  -- MiniLM 384d (운영시 e5-large 1024d 가능)
-- CREATE INDEX idx_jp_emb ON job_postings USING hnsw (embedding vector_cosine_ops);

-- ============================================================
-- users : 계정 (인증/PII 는 백엔드 소유 — 여기선 매칭에 필요한 최소만)
-- ============================================================
CREATE TABLE users (
                       id            bigserial PRIMARY KEY,
                       ext_ref       text UNIQUE,                 -- 백엔드 유저 식별자(선택)
                       created_at    timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- ① 선언적 선호 (declared) : 온보딩 최소입력 + 진행형 갱신. 1:1
--    → retrieve 하드필터(지역/경력/제외) + 임베딩 쿼리 재료
-- ============================================================
CREATE TABLE user_preferences (
                                  user_id            bigint PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                  interest_roles     text[] DEFAULT '{}',    -- 관심 직무 (필수 최소)
                                  career_stage       text CHECK (career_stage IN ('newbie','1_3','3plus')),  -- 경력 단계(필수)
                                  career_years       integer,                -- 연차(이력서/입력에서 확정되면 채움)
                                  interest_fields    text[] DEFAULT '{}',    -- 관심 분야(기획/개발/디자인/데이터)
                                  locations          text[] DEFAULT '{}',    -- 희망 지역
                                  remote_ok          boolean DEFAULT false,
                                  company_size_pref  text[] DEFAULT '{}',    -- 선호 기업규모
                                  preferred_free     text,                   -- 선호조건 자유입력(추천 근거 생성에 활용)
                                  excludes           text[] DEFAULT '{}',    -- 제외 조건(피드백 reason_code 가 누적 반영)
                                  updated_at         timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- ② 역량/증거 (capability) : 이력서/자소서. 원문 + 파싱 + 임베딩
--    → retrieve 대칭 의미매칭(문서 임베딩) + 추출스킬 부스트 + LLM 근거 컨텍스트
-- ============================================================
CREATE TABLE user_documents (
                                id            bigserial PRIMARY KEY,
                                user_id       bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                doc_type      text NOT NULL CHECK (doc_type IN ('resume','cover_letter','portfolio')),
                                raw_text      text,                        -- 파싱된 본문(민감정보 — 접근통제 필요)
                                file_url      text,                        -- 원본 파일(선택)
                                parsed        jsonb,                       -- 구조화 추출(연차/도메인/경력항목 등)
                                embedding     vector(384),                 -- 문서 임베딩(JD 와 대칭 매칭)
                                is_active     boolean NOT NULL DEFAULT true,
                                uploaded_at   timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_doc_user ON user_documents(user_id);

-- 통합 스킬 신호 (선언/이력서/성향 어디서 왔든 한 곳으로 정규화)
--   canonical_id = 원티드 skill_tag_ids 어휘 재사용 → 공고 스킬과 동일 축에서 매칭
CREATE TABLE user_skills (
                             user_id       bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             skill         text NOT NULL,               -- 정규화 스킬명 (예: React)
                             canonical_id  integer,                     -- 원티드 canonical id (있으면)
                             source        text NOT NULL CHECK (source IN ('declared','resume','test','feedback')),
                             weight        real NOT NULL DEFAULT 1.0,   -- 증거 강도(이력서>선언)
                             evidence      text,                        -- 근거 스니펫(이력서 문장 등)
                             updated_at    timestamptz NOT NULL DEFAULT now(),
                             PRIMARY KEY (user_id, skill, source)
);
CREATE INDEX idx_uskill_user ON user_skills(user_id);

-- ============================================================
-- ③ 잠재 성향 (disposition) : 직무 성향 테스트(게임). 소프트 부스트 + 타이브레이커
--    ※ 각 축은 반드시 관측가능한 공고/기업 피처에 매핑되어야 함(SIGNAL_DESIGN 4.3)
-- ============================================================
CREATE TABLE user_disposition (
                                  user_id       bigint PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                  test_version  text NOT NULL,
                                  axes          jsonb NOT NULL,              -- {stability_challenge:0.7, specialist_generalist:-0.2, ...} (-1~1)
                                  tags          text[] DEFAULT '{}',         -- 파생 태그(예: 도전지향, 스타트업선호)
                                  raw_answers   jsonb,
                                  completed_at  timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- ④ 행동 피드백 (behavioral / L5) : 개선 루프의 심장
--    → 좋아요/싫어요/저장/스킵/지원 + reason_code + 자유 코멘트
-- ============================================================
CREATE TABLE user_events (
                             id            bigserial PRIMARY KEY,
                             user_id       bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             posting_id    bigint REFERENCES job_postings(id) ON DELETE SET NULL,
                             rec_item_id   bigint,                      -- 어떤 추천으로 노출됐는지(아래 rec_items)
                             event_type    text NOT NULL CHECK (event_type IN
                                                                ('impression','open','click','save','skip','like','dislike',
                                                                 'apply_intent','apply')),
                             reason_code   text,                        -- 스킵/싫어요 사유(지역안맞음/직무안맞음/경력조건/이미지원함)
                             comment       text,                        -- 자유 코멘트(고신호 — LLM 해석)
                             created_at    timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_ev_user ON user_events(user_id, created_at DESC);
CREATE INDEX idx_ev_posting ON user_events(posting_id);

-- 피드백 누적 → 유저별 파생 상태(웜스타트 신호). 배치/실시간 갱신.
--   taste_vector : 저장/좋아요한 공고 임베딩의 가중 평균(취향 벡터)
--   pref_memory  : 코멘트/사유를 LLM이 요약한 '진화하는 선호'(선별기 컨텍스트로 주입)
CREATE TABLE user_signal_state (
                                   user_id        bigint PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                   taste_vector   vector(384),
                                   pref_memory    jsonb,                      -- {"likes":["React 관련 저장 많음"],"dislikes":["대기업 선호 낮음"]}
                                   event_count    integer NOT NULL DEFAULT 0, -- 콜드↔웜 전환 판단
                                   updated_at     timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- L4 추천 로깅 : LLM 선별 결과(무엇을·왜 노출했나) — 피드백 연결 + 이유 감사 + 평가
-- ============================================================
CREATE TABLE rec_bundles (
                             id            bigserial PRIMARY KEY,
                             user_id       bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                             bundle_date   date NOT NULL,               -- '오늘의 추천' 일자
                             created_at    timestamptz NOT NULL DEFAULT now(),
                             UNIQUE (user_id, bundle_date)
);
CREATE TABLE rec_items (
                           id            bigserial PRIMARY KEY,
                           bundle_id     bigint NOT NULL REFERENCES rec_bundles(id) ON DELETE CASCADE,
                           posting_id    bigint NOT NULL REFERENCES job_postings(id),
                           rank          integer NOT NULL,
                           fit_score     integer,                     -- 적합도(카드 표면 점수)
                           reason        text,                        -- LLM 추천 이유(한 줄)
                           fit_points    jsonb,                       -- 근거 배열
                           caution       text,                        -- 약점(연봉 미기재 등 — 숨기지 않음)
                           stage_scores  jsonb,                       -- {cosine, skill_overlap, disp, fb} 추적
                           model         text,
                           created_at    timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_ri_bundle ON rec_items(bundle_id);
CREATE INDEX idx_ri_posting ON rec_items(posting_id);

-- ============================================================
-- COMMENT (함정/핵심)
-- ============================================================
COMMENT ON TABLE  user_preferences  IS '신호①선언. retrieve 하드필터(지역/경력/제외) + 임베딩 쿼리 재료';
COMMENT ON TABLE  user_documents    IS '신호②역량. 이력서/자소서 원문+임베딩 → 대칭 의미매칭 + LLM 근거';
COMMENT ON TABLE  user_disposition  IS '신호③성향. 소프트 부스트/타이브레이커. 각 축은 공고 피처에 매핑 필수';
COMMENT ON TABLE  user_events       IS '신호④행동(L5). 개선 루프 입력. comment 는 LLM 해석 대상';
COMMENT ON COLUMN user_signal_state.taste_vector IS '저장/좋아요 공고 임베딩 가중평균 — 웜스타트 취향 벡터';
COMMENT ON COLUMN user_signal_state.pref_memory  IS '피드백을 LLM이 요약한 진화형 선호 — 선별기 프롬프트에 주입';
COMMENT ON COLUMN user_skills.canonical_id       IS '원티드 skill_tag_ids 어휘 재사용 → 공고 스킬과 동일 축 매칭';
