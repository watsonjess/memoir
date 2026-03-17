CREATE TABLE weekly_items (
    id          BIGSERIAL PRIMARY KEY,
    weekly_id   BIGINT    NOT NULL REFERENCES weekly(id) ON DELETE CASCADE,
    item_id     BIGINT    NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    added_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (weekly_id, item_id)
);