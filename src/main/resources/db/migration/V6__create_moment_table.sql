DROP TABLE IF EXISTS moments;

CREATE TABLE moments (
    id bigserial PRIMARY KEY,
    created_by bigint NOT NULL REFERENCES users(id),
    image_url varchar(255) NOT NULL,
    content text,
    location text,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);