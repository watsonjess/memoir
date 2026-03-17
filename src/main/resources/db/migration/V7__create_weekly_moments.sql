CREATE TABLE weekly_moments (
    id          BIGSERIAL PRIMARY KEY,
    weekly_id   BIGINT    NOT NULL REFERENCES weekly(id) ON DELETE CASCADE,
    moment_id     BIGINT    NOT NULL REFERENCES moments(id) ON DELETE CASCADE,
    added_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (weekly_id, moment_id)
);