-- Persist all domain timestamps with an explicit UTC offset.
-- Existing timestamp values are interpreted in the database session time zone.
DO $$
DECLARE
    target record;
BEGIN
    FOR target IN
        SELECT *
        FROM (VALUES
            ('companies', 'created_at'),
            ('companies', 'updated_at'),
            ('job_categories', 'created_at'),
            ('job_categories', 'updated_at'),
            ('regions', 'created_at'),
            ('regions', 'updated_at'),
            ('tech_stacks', 'created_at'),
            ('tech_stacks', 'updated_at'),
            ('users', 'withdrawn_at'),
            ('users', 'created_at'),
            ('users', 'updated_at'),
            ('jobs', 'created_at'),
            ('jobs', 'updated_at'),
            ('saved_jobs', 'created_at'),
            ('saved_jobs', 'updated_at'),
            ('user_profiles', 'job_test_completed_at'),
            ('user_profiles', 'onboarding_completed_at'),
            ('user_profiles', 'created_at'),
            ('user_profiles', 'updated_at'),
            ('user_consents', 'agreed_at'),
            ('user_consents', 'created_at'),
            ('user_consents', 'updated_at'),
            ('user_tech_stacks', 'created_at'),
            ('user_tech_stacks', 'updated_at'),
            ('user_job_categories', 'created_at'),
            ('user_job_categories', 'updated_at'),
            ('user_regions', 'created_at'),
            ('user_regions', 'updated_at'),
            ('user_withdrawals', 'requested_at'),
            ('user_withdrawals', 'scheduled_deletion_at'),
            ('user_withdrawals', 'restored_at'),
            ('user_withdrawals', 'created_at'),
            ('user_withdrawals', 'updated_at'),
            ('resumes', 'uploaded_at'),
            ('resumes', 'deleted_at'),
            ('resumes', 'created_at'),
            ('resumes', 'updated_at'),
            ('decks', 'created_at'),
            ('decks', 'updated_at'),
            ('cards', 'created_at'),
            ('cards', 'updated_at'),
            ('user_actions', 'created_at'),
            ('user_actions', 'updated_at'),
            ('notification_settings', 'created_at'),
            ('notification_settings', 'updated_at'),
            ('unsubscribe_feedbacks', 'created_at'),
            ('unsubscribe_feedbacks', 'updated_at'),
            ('personality_tests', 'created_at'),
            ('personality_tests', 'updated_at'),
            ('personality_test_answers', 'answered_at'),
            ('personality_test_answers', 'created_at'),
            ('personality_test_answers', 'updated_at')
        ) AS timestamp_columns(table_name, column_name)
    LOOP
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = current_schema()
              AND table_name = target.table_name
              AND column_name = target.column_name
              AND data_type = 'timestamp without time zone'
        ) THEN
            EXECUTE format(
                'ALTER TABLE %I ALTER COLUMN %I TYPE timestamptz USING %I AT TIME ZONE current_setting(''TimeZone'')',
                target.table_name,
                target.column_name,
                target.column_name
            );
        END IF;
    END LOOP;
END $$;
