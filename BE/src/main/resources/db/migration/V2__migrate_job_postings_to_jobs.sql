-- V2: job_postings 원문 데이터를 앱 서빙 테이블 jobs로 이관
-- ON CONFLICT DO NOTHING으로 멱등성 보장 (재실행 시 중복 삽입 없음)
--
-- 컬럼 매핑:
--   job_postings.position     → jobs.title
--   job_postings.confirm_time → jobs.posted_at
--   job_postings.due_time     → jobs.deadline_at
--   job_postings.is_remote    → jobs.remote_available
--   status: 'active' → ACTIVE, 마감 지남 → EXPIRED, 그 외 → REMOVED

INSERT INTO jobs (
    company_id,
    title,
    career_level,
    employment_type,
    remote_available,
    salary_disclosed,
    source,
    external_id,
    source_url,
    posted_at,
    deadline_at,
    status,
    location_full,
    intro,
    main_tasks,
    requirements,
    preferred_points,
    benefits,
    career_min,
    career_max,
    reward_total,
    thumbnail_url,
    skill_tags,
    skills_inferred,
    category_child,
    location_city,
    location_district,
    is_newbie,
    created_at,
    updated_at
)
SELECT
    jp.company_id,
    jp.position,
    CASE
        WHEN jp.is_newbie IS TRUE AND jp.career_min IS NULL THEN '신입'
        WHEN jp.career_min IS NOT NULL AND jp.career_max IS NOT NULL
            THEN jp.career_min || '~' || jp.career_max || '년'
        WHEN jp.career_min IS NOT NULL
            THEN jp.career_min || '년 이상'
        ELSE '경력무관'
    END,
    jp.employment_type,
    COALESCE(jp.is_remote, false),
    false,
    jp.source,
    jp.external_id,
    jp.source_url,
    jp.confirm_time,
    jp.due_time,
    CASE
        WHEN COALESCE(LOWER(jp.status), '') <> 'active'            THEN 'REMOVED'
        WHEN jp.due_time IS NOT NULL AND jp.due_time < NOW()        THEN 'EXPIRED'
        ELSE 'ACTIVE'
    END,
    jp.location_full,
    jp.intro,
    jp.main_tasks,
    jp.requirements,
    jp.preferred_points,
    jp.benefits,
    jp.career_min,
    jp.career_max,
    jp.reward_total,
    jp.thumbnail_url,
    jp.skill_tags,
    jp.skills_inferred,
    jp.category_child,
    jp.location_city,
    jp.location_district,
    jp.is_newbie,
    NOW(),
    NOW()
FROM job_postings jp
WHERE jp.source IS NOT NULL
  AND jp.external_id IS NOT NULL
  AND jp.position IS NOT NULL
ON CONFLICT ON CONSTRAINT uk_jobs_source_external_id DO NOTHING;
