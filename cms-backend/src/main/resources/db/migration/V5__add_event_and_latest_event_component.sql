-- Add events table
CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    catalog_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    sync_version INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_events_catalog FOREIGN KEY (catalog_id) REFERENCES catalogs(id)
);

-- Add latest_event_components table
CREATE TABLE latest_event_components (
    id BIGINT PRIMARY KEY,
    title VARCHAR(255),
    event_ids TEXT,
    CONSTRAINT fk_latest_event_components_base FOREIGN KEY (id) REFERENCES components(id) ON DELETE CASCADE
);
