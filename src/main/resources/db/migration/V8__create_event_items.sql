CREATE TABLE event_items (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT    NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    item_id     BIGINT    NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    added_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (event_id, item_id)
);