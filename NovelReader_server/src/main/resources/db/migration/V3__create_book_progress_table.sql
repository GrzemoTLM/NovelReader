CREATE TABLE book_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    chapter_index INT NOT NULL DEFAULT 0,
    offset_in_chapter INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_book
        FOREIGN KEY(book_id) REFERENCES books(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_book_user UNIQUE (book_id, user_id)
);
