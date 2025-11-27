-- Drop existing foreign key constraint if it exists
SET @dbname = DATABASE();
SET @tablename = 'media';
SET @constraintname = 'fk_media_incident';

SELECT @constraint_exists := COUNT(*)
FROM information_schema.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = @dbname
AND TABLE_NAME = @tablename
AND CONSTRAINT_NAME = @constraintname
AND CONSTRAINT_TYPE = 'FOREIGN KEY';

SET @sql = IF(@constraint_exists > 0, 
    CONCAT('ALTER TABLE `', @tablename, '` DROP FOREIGN KEY `', @constraintname, '`'),
    'SELECT "Foreign key does not exist" AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update the media table schema
ALTER TABLE media
MODIFY COLUMN id VARCHAR(36) NOT NULL,
MODIFY COLUMN customer_id VARCHAR(36) NULL,
MODIFY COLUMN incident_id VARCHAR(36) NULL,
MODIFY COLUMN created_by VARCHAR(36) NULL;

-- Re-add the foreign key constraint
ALTER TABLE media 
ADD CONSTRAINT fk_media_incident 
FOREIGN KEY (incident_id) REFERENCES incident(id);

-- Update the ID to be auto-generated if it's not already set
-- This assumes your application will set the ID, but ensures existing records have a valid UUID
UPDATE media SET id = UUID() WHERE id IS NULL OR id = '';
