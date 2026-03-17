CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE,
                       email VARCHAR(255) UNIQUE,
                       profile_image VARCHAR(255),
                       created_at TIMESTAMP,
                       firstname VARCHAR(255),
                       lastname VARCHAR(255)
);

CREATE TABLE friendships (
                             requester_id BIGINT REFERENCES users(id),
                             addressee_id BIGINT REFERENCES users(id),
                             status VARCHAR(50),
                             PRIMARY KEY (requester_id, addressee_id)
);