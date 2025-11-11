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
  fullText VARCHAR(512),
  PRIMARY KEY (id)
) ENGINE=InnoDB;

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

-- Add columns to b2b_unit if they don't exist
ALTER TABLE b2b_unit
  ADD COLUMN IF NOT EXISTS address_id BINARY(16) NULL,
  ADD COLUMN IF NOT EXISTS group_id BINARY(16) NULL,
  ADD COLUMN IF NOT EXISTS onboardedBy VARCHAR(32) NULL,
  ADD COLUMN IF NOT EXISTS approvedBy VARCHAR(100) NULL,
  ADD COLUMN IF NOT EXISTS approvedAt TIMESTAMP NULL;

-- Drop legacy address column if still present
ALTER TABLE b2b_unit DROP COLUMN IF EXISTS address;

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS ux_b2b_unit_address_id ON b2b_unit(address_id);
CREATE INDEX IF NOT EXISTS ix_b2b_unit_group_id ON b2b_unit(group_id);

-- Foreign keys (guarded by existing constraint names check is not trivial; attempt create)
ALTER TABLE b2b_unit
  ADD CONSTRAINT fk_b2b_unit_address
    FOREIGN KEY (address_id) REFERENCES address(id);

ALTER TABLE b2b_unit
  ADD CONSTRAINT fk_b2b_unit_group
    FOREIGN KEY (group_id) REFERENCES b2b_group(id);
