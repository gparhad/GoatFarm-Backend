-- Optional PostgreSQL hardening. NOT applied automatically: review, back up, then run by hand.
-- Hibernate (ddl-auto=update) already tries to create uk_goat_farm_tag from the @UniqueConstraint on Goat;
-- if your data contains duplicates that statement fails silently and the constraint is missing.

-- 1) Find duplicate tag numbers inside a farm (must return 0 rows before the unique constraint can exist)
SELECT farm_id, tag_number, COUNT(*) FROM goat GROUP BY farm_id, tag_number HAVING COUNT(*) > 1;

-- 2) Find goats with more than one active pregnancy (must return 0 rows before the index in step 3)
SELECT goat_tag_number, farm_id, COUNT(*)
FROM breeding_records WHERE pregnancy_status = 'PREGNANT'
GROUP BY goat_tag_number, farm_id HAVING COUNT(*) > 1;

-- 3) At most one active pregnancy per goat (the application serialises requests with a row lock as well;
--    this makes the rule hold even for writers that bypass the application)
CREATE UNIQUE INDEX IF NOT EXISTS uq_breeding_one_active_pregnancy
    ON breeding_records (goat_id) WHERE pregnancy_status = 'PREGNANT';

-- 4) Back-fill the denormalised farm_id on legacy vaccination rows (queries no longer depend on it, but it keeps the data tidy)
UPDATE vaccination_record vr SET farm_id = g.farm_id
FROM goat g WHERE vr.goat_id = g.goat_id AND vr.farm_id IS NULL;

-- 5) Optimistic locking (@Version) is NOT enabled in code: adding the column to existing rows needs a back-fill first.
--    When you are ready:  ALTER TABLE goat ADD COLUMN version BIGINT NOT NULL DEFAULT 0;  (same for other tables),
--    then add `@Version private long version;` to the entity.
