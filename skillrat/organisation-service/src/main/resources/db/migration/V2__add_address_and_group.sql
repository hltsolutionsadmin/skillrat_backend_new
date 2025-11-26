-- Create Address table
CREATE TABLE IF NOT EXISTS address (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  line1 VARCHAR(128),
  line2 VARCHAR(128),
  city VARCHAR(64),
  state VARCHAR(64),
  country VARCHAR(64),
  postalCode VARCHAR(16),
  `fullText` VARCHAR(512),
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Ensure address has createdDate/updatedDate columns (if table pre-existed without them)
SET @addr_has_createdDate := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'address' AND column_name = 'createdDate'
);
SET @addr_add_createdDate := IF(@addr_has_createdDate = 0,
  'ALTER TABLE address ADD COLUMN createdDate TIMESTAMP NOT NULL DEFAULT NOW()',
  'SELECT 1');
PREPARE stmt_addr_cd FROM @addr_add_createdDate; EXECUTE stmt_addr_cd; DEALLOCATE PREPARE stmt_addr_cd;

SET @addr_has_updatedDate := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'address' AND column_name = 'updatedDate'
);
SET @addr_add_updatedDate := IF(@addr_has_updatedDate = 0,
  'ALTER TABLE address ADD COLUMN updatedDate TIMESTAMP NOT NULL DEFAULT NOW()',
  'SELECT 1');
PREPARE stmt_addr_ud FROM @addr_add_updatedDate; EXECUTE stmt_addr_ud; DEALLOCATE PREPARE stmt_addr_ud;

-- Create B2B Group table
CREATE TABLE IF NOT EXISTS b2b_group (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  name VARCHAR(200) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- Ensure b2b_group has createdDate/updatedDate columns (if table pre-existed without them)
SET @grp_has_createdDate := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'b2b_group' AND column_name = 'createdDate'
);
SET @grp_add_createdDate := IF(@grp_has_createdDate = 0,
  'ALTER TABLE b2b_group ADD COLUMN createdDate TIMESTAMP NOT NULL DEFAULT NOW() AFTER id',
  'SELECT 1');
PREPARE stmt_grp_cd FROM @grp_add_createdDate; EXECUTE stmt_grp_cd; DEALLOCATE PREPARE stmt_grp_cd;

SET @grp_has_updatedDate := (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'b2b_group' AND column_name = 'updatedDate'
);
SET @grp_add_updatedDate := IF(@grp_has_updatedDate = 0,
  'ALTER TABLE b2b_group ADD COLUMN updatedDate TIMESTAMP NOT NULL DEFAULT NOW() AFTER createdDate',
  'SELECT 1');
PREPARE stmt_grp_ud FROM @grp_add_updatedDate; EXECUTE stmt_grp_ud; DEALLOCATE PREPARE stmt_grp_ud;

-- Add columns to b2b_unit if they don't exist
-- MySQL 5.7 compatible guarded column adds
-- address_id
SET @col_missing_address_id := (
  SELECT COUNT(*) = 0 FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'b2b_unit' AND column_name = 'address_id'
);
SET @sql_add_address_id := IF(@col_missing_address_id,
  'ALTER TABLE b2b_unit ADD COLUMN address_id BINARY(16) NULL',
  'SELECT 1'
);
PREPARE stmt_add_address_id FROM @sql_add_address_id; EXECUTE stmt_add_address_id; DEALLOCATE PREPARE stmt_add_address_id;

-- group_id
SET @col_missing_group_id := (
  SELECT COUNT(*) = 0 FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'b2b_unit' AND column_name = 'group_id'
);
SET @sql_add_group_id := IF(@col_missing_group_id,
  'ALTER TABLE b2b_unit ADD COLUMN group_id BINARY(16) NULL',
  'SELECT 1'
);
PREPARE stmt_add_group_id FROM @sql_add_group_id; EXECUTE stmt_add_group_id; DEALLOCATE PREPARE stmt_add_group_id;
-- onboardedBy
SET @col_missing_onboardedBy := (
  SELECT COUNT(*) = 0 FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'b2b_unit' AND column_name = 'onboardedBy'
);
SET @sql_add_onboardedBy := IF(@col_missing_onboardedBy,
  'ALTER TABLE b2b_unit ADD COLUMN onboardedBy VARCHAR(32) NULL',
  'SELECT 1'
);
PREPARE stmt_add_onboardedBy FROM @sql_add_onboardedBy; EXECUTE stmt_add_onboardedBy; DEALLOCATE PREPARE stmt_add_onboardedBy;

-- approvedBy
SET @col_missing_approvedBy := (
  SELECT COUNT(*) = 0 FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'b2b_unit' AND column_name = 'approvedBy'
);
SET @sql_add_approvedBy := IF(@col_missing_approvedBy,
  'ALTER TABLE b2b_unit ADD COLUMN approvedBy VARCHAR(100) NULL',
  'SELECT 1'
);
PREPARE stmt_add_approvedBy FROM @sql_add_approvedBy; EXECUTE stmt_add_approvedBy; DEALLOCATE PREPARE stmt_add_approvedBy;

-- approvedAt
SET @col_missing_approvedAt := (
  SELECT COUNT(*) = 0 FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'b2b_unit' AND column_name = 'approvedAt'
);
SET @sql_add_approvedAt := IF(@col_missing_approvedAt,
  'ALTER TABLE b2b_unit ADD COLUMN approvedAt TIMESTAMP NULL',
  'SELECT 1'
);
PREPARE stmt_add_approvedAt FROM @sql_add_approvedAt; EXECUTE stmt_add_approvedAt; DEALLOCATE PREPARE stmt_add_approvedAt;

-- Drop legacy address column if still present (guarded)
SET @addr_col_exists := (
  SELECT COUNT(*) FROM information_schema.columns c
  WHERE c.table_schema = DATABASE()
    AND c.table_name = 'b2b_unit'
    AND c.column_name = 'address'
);
SET @drop_addr_col_sql := IF(@addr_col_exists = 1,
  'ALTER TABLE b2b_unit DROP COLUMN address',
  'SELECT 1');
PREPARE stmt0 FROM @drop_addr_col_sql; EXECUTE stmt0; DEALLOCATE PREPARE stmt0;

-- Create ix_b2b_unit_address_id if not exists
SET @ix_addr_exists := (
  SELECT COUNT(*) FROM information_schema.statistics s
  WHERE s.table_schema = DATABASE()
    AND s.table_name = 'b2b_unit'
    AND s.index_name = 'ix_b2b_unit_address_id'
);
SET @create_ix_addr := IF(@ix_addr_exists = 0,
  'CREATE INDEX ix_b2b_unit_address_id ON b2b_unit(address_id)',
  'SELECT 1');
PREPARE stmtix1 FROM @create_ix_addr; EXECUTE stmtix1; DEALLOCATE PREPARE stmtix1;

-- Create ix_b2b_unit_group_id if not exists
SET @ix_grp_exists := (
  SELECT COUNT(*) FROM information_schema.statistics s
  WHERE s.table_schema = DATABASE()
    AND s.table_name = 'b2b_unit'
    AND s.index_name = 'ix_b2b_unit_group_id'
);
SET @create_ix_grp := IF(@ix_grp_exists = 0,
  'CREATE INDEX ix_b2b_unit_group_id ON b2b_unit(group_id)',
  'SELECT 1');
PREPARE stmtix2 FROM @create_ix_grp; EXECUTE stmtix2; DEALLOCATE PREPARE stmtix2;

-- Foreign keys (guarded by existing constraint names check is not trivial; attempt create)
-- Safely add fk_b2b_unit_address if not exists
SET @fk_exists := (
  SELECT COUNT(*) FROM information_schema.table_constraints tc
  WHERE tc.table_schema = DATABASE()
    AND tc.table_name = 'b2b_unit'
    AND tc.constraint_name = 'fk_b2b_unit_address'
);
SET @sql := IF(@fk_exists = 0,
  'ALTER TABLE b2b_unit ADD CONSTRAINT fk_b2b_unit_address FOREIGN KEY (address_id) REFERENCES address(id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Safely add fk_b2b_unit_group if not exists
SET @fk2_exists := (
  SELECT COUNT(*) FROM information_schema.table_constraints tc
  WHERE tc.table_schema = DATABASE()
    AND tc.table_name = 'b2b_unit'
    AND tc.constraint_name = 'fk_b2b_unit_group'
);
SET @sql2 := IF(@fk2_exists = 0,
  'ALTER TABLE b2b_unit ADD CONSTRAINT fk_b2b_unit_group FOREIGN KEY (group_id) REFERENCES b2b_group(id)',
  'SELECT 1');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;
