CREATE TABLE weekly (
    id          BIGSERIAL PRIMARY KEY,
    group_id    BIGINT       NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    week_start  TIMESTAMP    NOT NULL,
    send_date   TIMESTAMP    NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'open', -- open / sent
    title       VARCHAR(255),
    sent_at     TIMESTAMP,
    UNIQUE (group_id, week_start)
);