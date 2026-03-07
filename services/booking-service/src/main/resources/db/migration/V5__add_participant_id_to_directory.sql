ALTER TABLE participant_directory
    ADD COLUMN participant_id VARCHAR(64) NULL;

UPDATE participant_directory
SET participant_id = CONCAT(
        CASE identity
            WHEN 'TEACHER' THEN 'TCH-AUTO-'
            ELSE 'STD-AUTO-'
        END,
        LPAD(id, 8, '0')
    )
WHERE participant_id IS NULL OR participant_id = '';

ALTER TABLE participant_directory
    MODIFY participant_id VARCHAR(64) NOT NULL;

ALTER TABLE participant_directory
    ADD CONSTRAINT uk_participant_identity_participant_id UNIQUE (identity, participant_id);
