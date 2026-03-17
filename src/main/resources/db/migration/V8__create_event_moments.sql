CREATE TABLE event_moments (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT    NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    moment_id     BIGINT    NOT NULL REFERENCES moments(id) ON DELETE CASCADE,
    added_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (event_id, moment_id)
);