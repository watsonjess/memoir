CREATE TABLE memories (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          cover_image_url VARCHAR(255),
                          created_by BIGINT NOT NULL REFERENCES users(id),
                          created_at TIMESTAMP NOT NULL DEFAULT now(),
                          description TEXT,
                          pin_x FLOAT DEFAULT 0,
                          pin_y FLOAT DEFAULT 0
);

CREATE TABLE memory_members (
                                id BIGSERIAL PRIMARY KEY,
                                memory_id BIGINT NOT NULL REFERENCES memories(id) ON DELETE CASCADE,
                                user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                role VARCHAR(20) DEFAULT 'contributor',
                                joined_at TIMESTAMP NOT NULL DEFAULT now(),
                                UNIQUE(memory_id, user_id)
);

CREATE TABLE thoughts (
                          id BIGSERIAL PRIMARY KEY,
                          memory_id BIGINT NOT NULL REFERENCES memories(id) ON DELETE CASCADE,
                          created_by BIGINT NOT NULL REFERENCES users(id),
                          content TEXT,
                          image_url VARCHAR(255),
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);