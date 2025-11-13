-- Seed sample data for B2BGroup, Address, and B2BUnit
-- This script assumes MySQL 8

-- Variables
SET @tenant := 'demo-tenant';
SET @now := NOW();
SET @actor := 'flyway-seed';
-- Generate UUIDs and keep both string and binary formats for readability
SET @grp1_uuid := UUID();
SET @grp1_id := UNHEX(REPLACE(@grp1_uuid, '-', ''));
SET @grp2_uuid := UUID();
SET @grp2_id := UNHEX(REPLACE(@grp2_uuid, '-', ''));
SET @addr1_uuid := UUID();
SET @addr1_id := UNHEX(REPLACE(@addr1_uuid, '-', ''));
SET @addr2_uuid := UUID();
SET @addr2_id := UNHEX(REPLACE(@addr2_uuid, '-', ''));
SET @addr3_uuid := UUID();
SET @addr3_id := UNHEX(REPLACE(@addr3_uuid, '-', ''));
SET @unit1_uuid := UUID();
SET @unit1_id := UNHEX(REPLACE(@unit1_uuid, '-', ''));
SET @unit2_uuid := UUID();
SET @unit2_id := UNHEX(REPLACE(@unit2_uuid, '-', ''));
SET @unit3_uuid := UUID();
SET @unit3_id := UNHEX(REPLACE(@unit3_uuid, '-', ''));

-- Insert Groups
INSERT INTO b2b_group (id, createdDate, updatedDate, createdBy, updatedBy, tenantId, name)
VALUES
  (@grp1_id, @now, @now, @actor, @actor, @tenant, 'Aditya Group'),
  (@grp2_id, @now, @now, @actor, @actor, @tenant, 'Global Tech Group');
-- Insert Addresses
INSERT INTO address (id, createdDate, updatedDate, createdBy, updatedBy, tenantId, line1, line2, city, state, country, postalCode, `fullText`)
VALUES
  (@addr1_id, @now, @now, @actor, @actor, @tenant,
   'Plot 10, Main road', 'Near City Mall', 'Hyderabad', 'Telangana', 'IN', '500081',
   'Plot 10, Main road, Near City Mall, Hyderabad, Telangana 500081, IN'),
  (@addr2_id, @now, @now, @actor, @actor, @tenant,
   'Sector 21', 'Opp. Central Park', 'Mumbai', 'Maharashtra', 'IN', '400001',
   'Sector 21, Opp. Central Park, Mumbai, Maharashtra 400001, IN'),
  (@addr3_id, @now, @now, @actor, @actor, @tenant,
   '123 Business Ave', 'Floor 5', 'Bengaluru', 'Karnataka', 'IN', '560001',
   '123 Business Ave, Floor 5, Bengaluru, Karnataka 560001, IN');

-- Insert Units (types and statuses are stored as strings)
INSERT INTO b2b_unit (
  id, createdDate, updatedDate, createdBy, updatedBy, tenantId,
  name, type, status, contactEmail, contactPhone, website, onboardedBy, approvedBy, approvedAt, address_id, group_id
) VALUES
  (@unit1_id, @now, @now, @actor, @actor, @tenant,
   'Aditya College - Branch A', 'COLLEGE', 'APPROVED', 'branchA@aditya.edu', '9999990001', 'https://aditya.edu/branch-a', 'ADMIN', 'seed', @now, @addr1_id, @grp1_id),
  (@unit2_id, @now, @now, @actor, @actor, @tenant,
   'Aditya College - Branch B', 'COLLEGE', 'PENDING_APPROVAL', 'branchB@aditya.edu', '9999990002', 'https://aditya.edu/branch-b', 'SELF', NULL, NULL, @addr2_id, @grp1_id),
  (@unit3_id, @now, @now, @actor, @actor, @tenant,
   'Global Tech - Bengaluru', 'COMPANY', 'APPROVED', 'contact@globaltech.com', '8888887777', 'https://globaltech.com', 'ADMIN', 'seed', @now, @addr3_id, @grp2_id);
