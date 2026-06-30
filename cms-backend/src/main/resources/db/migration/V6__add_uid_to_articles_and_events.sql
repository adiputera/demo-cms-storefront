-- Add stable uid column to articles for syncKey matching between STAGED and ONLINE
ALTER TABLE articles ADD COLUMN uid VARCHAR(36);
UPDATE articles SET uid = gen_random_uuid()::TEXT WHERE uid IS NULL;
ALTER TABLE articles ALTER COLUMN uid SET NOT NULL;

-- Add stable uid column to events for syncKey matching between STAGED and ONLINE
ALTER TABLE events ADD COLUMN uid VARCHAR(36);
UPDATE events SET uid = gen_random_uuid()::TEXT WHERE uid IS NULL;
ALTER TABLE events ALTER COLUMN uid SET NOT NULL;
