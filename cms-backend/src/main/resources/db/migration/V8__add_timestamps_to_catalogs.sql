-- Add audit timestamps to catalogs table since Catalog now extends ItemModel
ALTER TABLE catalogs ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE catalogs ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
