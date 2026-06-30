-- Add articles table
CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    sync_version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_articles_catalog FOREIGN KEY (catalog_id) REFERENCES catalogs(id)
);

-- Add trending_article_components table
CREATE TABLE trending_article_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    article_ids TEXT,
    CONSTRAINT fk_trending_article_components_id FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);
