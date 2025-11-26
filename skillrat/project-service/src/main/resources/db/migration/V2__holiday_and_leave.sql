-- V2: Holiday calendars, leave management, and time_entry.entryType; project.holidayCalendarId

-- Add entryType to time_entry
ALTER TABLE time_entry
  ADD COLUMN IF NOT EXISTS entryType VARCHAR(16) NOT NULL DEFAULT 'WORK';

-- Add holidayCalendarId to project
ALTER TABLE project
  ADD COLUMN IF NOT EXISTS holidayCalendarId BINARY(16) NULL;

-- Holiday Calendar tables
CREATE TABLE IF NOT EXISTS holiday_calendar (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  name VARCHAR(150) NOT NULL,
  b2bUnitId BINARY(16) NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS holiday_day (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  calendar_id BINARY(16) NOT NULL,
  date DATE NOT NULL,
  name VARCHAR(150) NOT NULL,
  optionalHoliday BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  CONSTRAINT fk_hday_calendar FOREIGN KEY (calendar_id) REFERENCES holiday_calendar(id)
) ENGINE=InnoDB;
CREATE UNIQUE INDEX IF NOT EXISTS ux_holiday_day_calendar_date ON holiday_day(calendar_id, date);

-- Leave Management tables
CREATE TABLE IF NOT EXISTS leave_balance (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  employeeId BINARY(16) NOT NULL,
  b2bUnitId BINARY(16) NOT NULL,
  year INT NOT NULL,
  type VARCHAR(32) NOT NULL,
  allocated DECIMAL(6,2) NOT NULL,
  consumed DECIMAL(6,2) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;
CREATE UNIQUE INDEX IF NOT EXISTS ux_leave_balance_emp_unit_year_type ON leave_balance(employeeId, b2bUnitId, year, type);

CREATE TABLE IF NOT EXISTS leave_request (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  employeeId BINARY(16) NOT NULL,
  b2bUnitId BINARY(16) NOT NULL,
  type VARCHAR(32) NOT NULL,
  fromDate DATE NOT NULL,
  toDate DATE NOT NULL,
  perDayHours DECIMAL(5,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  approverId BINARY(16) NULL,
  decisionAt TIMESTAMP NULL,
  note VARCHAR(300) NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;
