CREATE TABLE group_members (
     id          BIGSERIAL PRIMARY KEY,
     group_id    BIGINT      NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
     user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
     role        VARCHAR(20) NOT NULL DEFAULT 'member', -- owner / member
     status      VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending / joined
     joined_at   TIMESTAMP   NOT NULL DEFAULT NOW(),
     UNIQUE (group_id, user_id)
);