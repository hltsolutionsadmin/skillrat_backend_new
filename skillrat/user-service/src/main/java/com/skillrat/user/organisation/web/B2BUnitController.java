package com.skillrat.user.organisation.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillrat.user.organisation.domain.Address;
import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.service.B2BUnitService;
import com.skillrat.user.organisation.web.dto.AdminOnboardRequest;
import com.skillrat.user.organisation.web.dto.SelfOnboardRequest;
import com.skillrat.user.organisation.web.mapper.OnboardingMapper;

@RestController
@RequestMapping("/api/b2b")
@Validated
public class B2BUnitController {

    private final B2BUnitService service;

    public B2BUnitController(B2BUnitService service) { this.service = service; }

    @PostMapping("/onboard/self")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<B2BUnit> selfOnboard(@RequestBody @Validated SelfOnboardRequest req) {
        Address addr = OnboardingMapper.toEntity(req.getAddress());
        B2BUnit unit = service.selfSignup(req.getName(), req.getType(), req.getContactEmail(), req.getContactPhone(), req.getWebsite(), addr, req.getGroupName());
        return ResponseEntity.ok(unit);
    }

    @PostMapping("/onboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<B2BUnit> adminOnboard(@RequestBody @Validated AdminOnboardRequest req) {
        Address addr = OnboardingMapper.toEntity(req.getAddress());
        B2BUnit unit = service.adminOnboard(
                req.getName(), req.getType(), req.getContactEmail(), req.getContactPhone(), req.getWebsite(), addr, req.getGroupName(), req.getApprover(),
                req.getAdminFirstName(), req.getAdminLastName(), req.getAdminEmail(), req.getAdminMobile());
        return ResponseEntity.ok(unit);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable("id") UUID id, @RequestBody Map<String, String> body) {
        String approver = body != null ? body.getOrDefault("approver", "skillrat-admin") : "skillrat-admin";
        return service.approve(id, approver)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<B2BUnit> pending() {
        return service.listPending();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        return service.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
