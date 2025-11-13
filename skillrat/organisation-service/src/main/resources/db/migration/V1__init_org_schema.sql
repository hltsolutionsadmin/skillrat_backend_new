-- Initial baseline schema for organisation-service
-- Creates b2b_unit table if it does not already exist so later migrations can ALTER it safely.

CREATE TABLE IF NOT EXISTS b2b_unit (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  name VARCHAR(200) NOT NULL,
  type VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  contactEmail VARCHAR(255),
  contactPhone VARCHAR(32),
  website VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB;
