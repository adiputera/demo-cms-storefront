-- Ensure page_slot_order is never null for existing and future slots
-- First, fix any remaining nulls
UPDATE slots SET page_slot_order = subquery.row_number - 1
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY page_id ORDER BY id) as row_number
    FROM slots
    WHERE page_slot_order IS NULL
) AS subquery
WHERE slots.id = subquery.id;

-- Add NOT NULL constraint and default value
ALTER TABLE slots ALTER COLUMN page_slot_order SET NOT NULL;
ALTER TABLE slots ALTER COLUMN page_slot_order SET DEFAULT 0;
