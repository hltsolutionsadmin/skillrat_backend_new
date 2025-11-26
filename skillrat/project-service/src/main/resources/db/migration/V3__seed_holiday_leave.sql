-- Seed holiday calendars, holidays, and leave balances
-- Assumes MySQL 8+

SET @tenant := 'demo-tenant';
SET @now := NOW();
SET @actor := 'flyway-seed';

-- Sample IDs
SET @cal_uuid := UUID();
SET @cal_id := UNHEX(REPLACE(@cal_uuid, '-', ''));

-- Example B2B and Employee IDs (replace if needed)
SET @b2b_uuid := '11111111-1111-1111-1111-111111111111';
SET @b2b_id := UNHEX(REPLACE(@b2b_uuid, '-', ''));
SET @emp_uuid := '22222222-2222-2222-2222-222222222222';
SET @emp_id := UNHEX(REPLACE(@emp_uuid, '-', ''));

-- Create Calendar
INSERT INTO holiday_calendar (id, createdDate, updatedDate, createdBy, updatedBy, tenantId, name, b2bUnitId)
VALUES
  (@cal_id, @now, @now, @actor, @actor, @tenant, 'India 2025 Public Holidays', @b2b_id);

-- Add Holidays
SET @day1_id := UNHEX(REPLACE(UUID(), '-', ''));
SET @day2_id := UNHEX(REPLACE(UUID(), '-', ''));
SET @day3_id := UNHEX(REPLACE(UUID(), '-', ''));

INSERT INTO holiday_day (id, createdDate, updatedDate, createdBy, updatedBy, tenantId, calendar_id, date, name, optionalHoliday)
VALUES
  (@day1_id, @now, @now, @actor, @actor, @tenant, @cal_id, DATE '2025-01-26', 'Republic Day', FALSE),
  (@day2_id, @now, @now, @actor, @actor, @tenant, @cal_id, DATE '2025-08-15', 'Independence Day', FALSE),
  (@day3_id, @now, @now, @actor, @actor, @tenant, @cal_id, DATE '2025-10-02', 'Gandhi Jayanti', FALSE);

-- Seed Leave Balance for employee
SET @lb_id := UNHEX(REPLACE(UUID(), '-', ''));
INSERT INTO leave_balance (id, createdDate, updatedDate, createdBy, updatedBy, tenantId,
                           employeeId, b2bUnitId, year, type, allocated, consumed)
VALUES
  (@lb_id, @now, @now, @actor, @actor, @tenant, @emp_id, @b2b_id, 2025, 'VACATION', 24.00, 0.00);
