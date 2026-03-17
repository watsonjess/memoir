CREATE TABLE users (
    id            bigserial       PRIMARY KEY,
    username      varchar(50)     NOT NULL UNIQUE,
    email         varchar(255)    NOT NULL UNIQUE,
    profile_image varchar(255),
    created_at    timestamptz     NOT NULL DEFAULT now(),
    firstname     varchar(255),
    lastname      varchar(255)
);