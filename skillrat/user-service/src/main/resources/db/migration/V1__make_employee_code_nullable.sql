-- Make employee_code nullable to support SINGLE_TABLE inheritance base User rows
ALTER TABLE users
  MODIFY COLUMN employee_code VARCHAR(64) NULL;
