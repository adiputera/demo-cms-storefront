-- V4: Migrate from One-to-Many to Many-to-Many for Slots and Components

-- 1. Create the join table
CREATE TABLE slot_components (
    slot_id BIGINT NOT NULL,
    component_id BIGINT NOT NULL,
    sort_order INTEGER NOT NULL,
    
    CONSTRAINT fk_sc_slot FOREIGN KEY (slot_id) REFERENCES slots(id) ON DELETE CASCADE,
    CONSTRAINT fk_sc_component FOREIGN KEY (component_id) REFERENCES components(id) ON DELETE CASCADE,
    
    PRIMARY KEY (slot_id, component_id)
);

CREATE INDEX idx_slot_components_order ON slot_components(slot_id, sort_order);

COMMENT ON TABLE slot_components IS 'Join table for many-to-many relationship between slots and components';

-- 2. Migrate existing data
INSERT INTO slot_components (slot_id, component_id, sort_order)
SELECT slot_id, id, sort_order FROM components;

-- 3. Drop old index
DROP INDEX IF EXISTS idx_components_slot_order;

-- 4. Drop old constraint
ALTER TABLE components DROP CONSTRAINT IF EXISTS fk_components_slot;

-- 5. Drop old columns
ALTER TABLE components DROP COLUMN slot_id;
ALTER TABLE components DROP COLUMN sort_order;
