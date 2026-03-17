DROP TABLE IF EXISTS moments;

CREATE TABLE moments (
    id bigserial PRIMARY KEY,
    created_by bigint NOT NULL REFERENCES users(id),
    type varchar(255) NOT NULL CHECK (type IN ('PHOTO', 'NOTE')),
    content text,
    image_url varchar(255),
    image_caption varchar(255),
    location text,
    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT check_photo CHECK (type != 'PHOTO' OR image_url IS NOT NULL),
    CONSTRAINT check_note  CHECK (type != 'NOTE'  OR content  IS NOT NULL)
);