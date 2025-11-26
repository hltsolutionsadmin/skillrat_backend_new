-- First, drop the foreign key constraint if it exists
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

-- Update media table to use VARCHAR(36) for incident_id to match UUID string format
ALTER TABLE media 
MODIFY COLUMN incident_id VARCHAR(36) NULL,
MODIFY COLUMN customer_id VARCHAR(36) NULL,
MODIFY COLUMN created_by VARCHAR(36) NULL;

-- Re-add the foreign key constraint
ALTER TABLE media 
ADD CONSTRAINT fk_media_incident 
FOREIGN KEY (incident_id) REFERENCES incident(id);
