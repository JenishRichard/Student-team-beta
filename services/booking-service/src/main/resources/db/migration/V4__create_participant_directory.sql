CREATE TABLE IF NOT EXISTS participant_directory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    identity VARCHAR(16) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_participant_identity_email UNIQUE (identity, email),
    CONSTRAINT chk_participant_identity CHECK (identity IN ('TEACHER', 'STUDENT'))
);
