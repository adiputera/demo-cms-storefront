-- Add page_slot_order column to support ordering of slots within a page
ALTER TABLE slots ADD COLUMN page_slot_order INTEGER;

-- Initialize existing slots with their current order
-- This assigns sequential order values based on the slot ID
UPDATE slots SET page_slot_order = subquery.row_number - 1
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY page_id ORDER BY id) as row_number
    FROM slots
) AS subquery
WHERE slots.id = subquery.id;
