CREATE TABLE items (
    id              BIGSERIAL PRIMARY KEY,
    created_by      BIGINT       NOT NULL REFERENCES users(id),
    type            VARCHAR(20)  NOT NULL, -- PHOTO / NOTE
    content         TEXT,                  -- note text or link URL
    image_url       VARCHAR(255),          -- if PHOTO
    image_caption   VARCHAR(255),          -- if PHOTO
    location        TEXT,                  -- optional, for map stretch goal
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);