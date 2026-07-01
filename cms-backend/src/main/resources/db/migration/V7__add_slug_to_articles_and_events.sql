-- Add slug column to articles and events
ALTER TABLE articles ADD COLUMN slug VARCHAR(255);
ALTER TABLE events ADD COLUMN slug VARCHAR(255);

-- Create top_event_components table
CREATE TABLE top_event_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    event_id VARCHAR(36),
    CONSTRAINT fk_top_event_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);

