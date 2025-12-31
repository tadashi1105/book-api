CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL CHECK (price >= 0),
    publication_status VARCHAR(50) NOT NULL CHECK (publication_status IN ('PUBLISHED', 'UNPUBLISHED'))
);
