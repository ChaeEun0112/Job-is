-- Keep long-form job fields aligned with the JPA TEXT mappings.
-- Existing databases may have been created before Flyway and can still contain varchar columns.
ALTER TABLE job_postings
    ALTER COLUMN source_url TYPE text,
    ALTER COLUMN position TYPE text,
    ALTER COLUMN intro TYPE text,
    ALTER COLUMN main_tasks TYPE text,
    ALTER COLUMN requirements TYPE text,
    ALTER COLUMN preferred_points TYPE text,
    ALTER COLUMN benefits TYPE text,
    ALTER COLUMN location_full TYPE text,
    ALTER COLUMN hire_rounds TYPE text,
    ALTER COLUMN thumbnail_url TYPE text;

ALTER TABLE jobs
    ALTER COLUMN title TYPE text,
    ALTER COLUMN source_url TYPE text,
    ALTER COLUMN location_full TYPE text,
    ALTER COLUMN intro TYPE text,
    ALTER COLUMN main_tasks TYPE text,
    ALTER COLUMN requirements TYPE text,
    ALTER COLUMN preferred_points TYPE text,
    ALTER COLUMN benefits TYPE text,
    ALTER COLUMN reward_total TYPE text,
    ALTER COLUMN thumbnail_url TYPE text,
    ALTER COLUMN skills TYPE text,
    ALTER COLUMN categories TYPE text,
    ALTER COLUMN embedding TYPE text;
