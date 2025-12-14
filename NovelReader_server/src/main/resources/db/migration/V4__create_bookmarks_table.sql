CREATE TABLE bookmarks (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    chapter_index INTEGER NOT NULL,
    character_offset INTEGER NOT NULL,
    progress_percent DOUBLE PRECISION,
    title VARCHAR(255),
    note VARCHAR(1000),
    text_snippet VARCHAR(500),
    color VARCHAR(7),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);
CREATE INDEX idx_bookmarks_book_id ON bookmarks(book_id);
CREATE INDEX idx_bookmarks_user_book ON bookmarks(user_id, book_id);

