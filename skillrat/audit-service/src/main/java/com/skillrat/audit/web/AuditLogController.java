package com.skillrat.audit.web;

import com.skillrat.audit.domain.AuditLog;
import com.skillrat.audit.repo.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit/logs")
public class AuditLogController {

    private final AuditLogRepository repo;

    public AuditLogController(AuditLogRepository repo) { this.repo = repo; }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody AuditLogRequest req) {
        AuditLog log = new AuditLog();
        log.setServiceName(req.serviceName);
        log.setEntityType(req.entityType);
        log.setEntityId(req.entityId);
        log.setAction(req.action);
        log.setFieldName(req.fieldName);
        log.setOldValue(req.oldValue);
        log.setNewValue(req.newValue);
        log.setChangedBy(req.changedBy);
        log.setChangedAt(req.changedAt != null ? req.changedAt : java.time.Instant.now());
        log.setTenantId(req.tenantId);
        repo.save(log);
        return ResponseEntity.accepted().build();
    }

    public static class AuditLogRequest {
        public String serviceName;
        public String entityType;
        public String entityId;
        public String action;
        public String fieldName;
        public String oldValue;
        public String newValue;
        public String changedBy;
        public java.time.Instant changedAt;
        public String tenantId;
    }
}
