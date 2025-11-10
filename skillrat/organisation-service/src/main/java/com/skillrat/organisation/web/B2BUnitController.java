package com.skillrat.organisation.web;

import com.skillrat.organisation.domain.B2BUnit;
import com.skillrat.organisation.domain.B2BUnitType;
import com.skillrat.organisation.service.B2BUnitService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/b2b")
@Validated
public class B2BUnitController {

    private final B2BUnitService service;

    public B2BUnitController(B2BUnitService service) { this.service = service; }

    @PostMapping("/onboard/self")
    public ResponseEntity<B2BUnit> selfOnboard(@RequestBody @Validated SelfOnboardRequest req) {
        B2BUnit unit = service.selfSignup(req.name, req.type, req.contactEmail, req.contactPhone, req.website, req.address);
        return ResponseEntity.ok(unit);
    }

    @PostMapping("/onboard/admin")
    public ResponseEntity<B2BUnit> adminOnboard(@RequestBody @Validated AdminOnboardRequest req) {
        B2BUnit unit = service.adminOnboard(req.name, req.type, req.contactEmail, req.contactPhone, req.website, req.address, req.approver);
        return ResponseEntity.ok(unit);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable("id") UUID id, @RequestBody Map<String, String> body) {
        String approver = body != null ? body.getOrDefault("approver", "skillrat-admin") : "skillrat-admin";
        return service.approve(id, approver)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    public List<B2BUnit> pending() {
        return service.listPending();
    }

    public static class SelfOnboardRequest {
        @NotBlank public String name;
        @NotNull public B2BUnitType type;
        @Email public String contactEmail;
        public String contactPhone;
        public String website;
        public String address;
    }

    public static class AdminOnboardRequest extends SelfOnboardRequest {
        public String approver;
    }
}
