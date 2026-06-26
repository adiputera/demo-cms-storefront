CREATE TABLE latest_article_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    article_count INTEGER NOT NULL DEFAULT 5,
    CONSTRAINT fk_latest_article_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);
