CREATE TABLE moment_groups (
                               moment_id BIGINT NOT NULL REFERENCES moments(id) ON DELETE CASCADE,
                               group_id  BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                               PRIMARY KEY (moment_id, group_id)
);