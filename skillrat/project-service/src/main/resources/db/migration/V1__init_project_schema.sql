-- Initial schema for project-service
-- Requires MySQL 8.0+

-- Project
CREATE TABLE IF NOT EXISTS project (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  name VARCHAR(200) NOT NULL,
  code VARCHAR(64) UNIQUE,
  category VARCHAR(32) NOT NULL,
  b2bUnitId BINARY(16) NOT NULL,
  startDate DATE NULL,
  endDate DATE NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- WBS Element
CREATE TABLE IF NOT EXISTS wbs_element (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  project_id BINARY(16) NOT NULL,
  name VARCHAR(200) NOT NULL,
  code VARCHAR(64) UNIQUE,
  startDate DATE NULL,
  endDate DATE NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_wbs_project FOREIGN KEY (project_id) REFERENCES project(id)
) ENGINE=InnoDB;

-- Project Member
CREATE TABLE IF NOT EXISTS project_member (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  project_id BINARY(16) NOT NULL,
  employeeId BINARY(16) NOT NULL,
  role VARCHAR(32) NOT NULL,
  reportingManagerId BINARY(16) NULL,
  startDate DATE NULL,
  endDate DATE NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  CONSTRAINT fk_member_project FOREIGN KEY (project_id) REFERENCES project(id)
) ENGINE=InnoDB;
CREATE INDEX ix_member_project ON project_member(project_id);
CREATE INDEX ix_member_employee ON project_member(employeeId);

-- WBS Allocation (member assigned to WBS with dates)
CREATE TABLE IF NOT EXISTS wbs_allocation (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  member_id BINARY(16) NOT NULL,
  wbs_id BINARY(16) NOT NULL,
  startDate DATE NULL,
  endDate DATE NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  CONSTRAINT fk_alloc_member FOREIGN KEY (member_id) REFERENCES project_member(id),
  CONSTRAINT fk_alloc_wbs FOREIGN KEY (wbs_id) REFERENCES wbs_element(id)
) ENGINE=InnoDB;
CREATE UNIQUE INDEX ux_alloc_member_wbs ON wbs_allocation(member_id, wbs_id, active);

-- Time Entry
CREATE TABLE IF NOT EXISTS time_entry (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  project_id BINARY(16) NOT NULL,
  wbs_id BINARY(16) NOT NULL,
  member_id BINARY(16) NOT NULL,
  employeeId BINARY(16) NOT NULL,
  workDate DATE NOT NULL,
  hours DECIMAL(5,2) NOT NULL,
  status VARCHAR(32) NOT NULL,
  notes VARCHAR(500) NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_te_project FOREIGN KEY (project_id) REFERENCES project(id),
  CONSTRAINT fk_te_wbs FOREIGN KEY (wbs_id) REFERENCES wbs_element(id),
  CONSTRAINT fk_te_member FOREIGN KEY (member_id) REFERENCES project_member(id)
) ENGINE=InnoDB;
CREATE INDEX ix_te_member_date ON time_entry(member_id, workDate);
CREATE INDEX ix_te_wbs_date ON time_entry(wbs_id, workDate);

-- Time Entry Approval (multiple approvers allowed)
CREATE TABLE IF NOT EXISTS time_entry_approval (
  id BINARY(16) NOT NULL,
  createdDate TIMESTAMP NOT NULL,
  updatedDate TIMESTAMP NOT NULL,
  createdBy VARCHAR(255) NOT NULL,
  updatedBy VARCHAR(255) NOT NULL,
  tenantId VARCHAR(255) NOT NULL,
  time_entry_id BINARY(16) NOT NULL,
  approverId BINARY(16) NOT NULL,
  approvedAt TIMESTAMP NOT NULL,
  approverNote VARCHAR(200) NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_tea_entry FOREIGN KEY (time_entry_id) REFERENCES time_entry(id)
) ENGINE=InnoDB;
CREATE INDEX ix_tea_entry ON time_entry_approval(time_entry_id);

-- Enforce workDate within WBS start/end if provided
ALTER TABLE time_entry
  ADD CONSTRAINT chk_te_within_wbs_dates
  CHECK (
    (SELECT 1 FROM wbs_element w
      WHERE w.id = time_entry.wbs_id
        AND (w.startDate IS NULL OR time_entry.workDate >= w.startDate)
        AND (w.endDate IS NULL OR time_entry.workDate <= w.endDate)
    ) = 1
  );

-- Enforce that member has an active allocation on the WBS and date within allocation
-- Use triggers for complex validation
DELIMITER $$
CREATE TRIGGER trg_time_entry_before_ins
BEFORE INSERT ON time_entry FOR EACH ROW
BEGIN
  DECLARE cnt INT DEFAULT 0;
  SELECT COUNT(*) INTO cnt
  FROM wbs_allocation a
  WHERE a.member_id = NEW.member_id
    AND a.wbs_id = NEW.wbs_id
    AND a.active = TRUE
    AND (a.startDate IS NULL OR NEW.workDate >= a.startDate)
    AND (a.endDate IS NULL OR NEW.workDate <= a.endDate);
  IF cnt = 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No active WBS allocation for member on given date';
  END IF;
END$$
DELIMITER ;

DELIMITER $$
CREATE TRIGGER trg_time_entry_before_upd
BEFORE UPDATE ON time_entry FOR EACH ROW
BEGIN
  DECLARE cnt INT DEFAULT 0;
  SELECT COUNT(*) INTO cnt
  FROM wbs_allocation a
  WHERE a.member_id = NEW.member_id
    AND a.wbs_id = NEW.wbs_id
    AND a.active = TRUE
    AND (a.startDate IS NULL OR NEW.workDate >= a.startDate)
    AND (a.endDate IS NULL OR NEW.workDate <= a.endDate);
  IF cnt = 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No active WBS allocation for member on given date';
  END IF;
END$$
DELIMITER ;
