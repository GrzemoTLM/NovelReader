CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    description TEXT,
    file_path VARCHAR(1024) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL
);
