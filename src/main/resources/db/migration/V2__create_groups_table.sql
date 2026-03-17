CREATE TABLE groups (
     id          BIGSERIAL PRIMARY KEY,
     name        VARCHAR(255) NOT NULL,
     description TEXT,
     type        VARCHAR(20)  NOT NULL DEFAULT 'weekly', -- weekly / event
     created_by  BIGINT       NOT NULL REFERENCES users(id),
     created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);