package com.skillrat.organisation.web;

import com.skillrat.organisation.domain.B2BUnit;
import com.skillrat.organisation.domain.B2BUnitType;
import com.skillrat.organisation.service.B2BUnitService;
import com.skillrat.organisation.domain.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<B2BUnit> selfOnboard(@RequestBody @Validated SelfOnboardRequest req) {
        Address addr = null;
        if (req.address != null) {
            addr = new Address();
            addr.setLine1(req.address.line1);
            addr.setLine2(req.address.line2);
            addr.setCity(req.address.city);
            addr.setState(req.address.state);
            addr.setCountry(req.address.country);
            addr.setPostalCode(req.address.postalCode);
            addr.setFullText(req.address.fullText);
        }
        B2BUnit unit = service.selfSignup(req.name, req.type, req.contactEmail, req.contactPhone, req.website, addr, req.groupName);
        return ResponseEntity.ok(unit);
    }

    @PostMapping("/onboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<B2BUnit> adminOnboard(@RequestBody @Validated AdminOnboardRequest req) {
        Address addr = null;
        if (req.address != null) {
            addr = new Address();
            addr.setLine1(req.address.line1);
            addr.setLine2(req.address.line2);
            addr.setCity(req.address.city);
            addr.setState(req.address.state);
            addr.setCountry(req.address.country);
            addr.setPostalCode(req.address.postalCode);
            addr.setFullText(req.address.fullText);
        }
        B2BUnit unit = service.adminOnboard(
                req.name, req.type, req.contactEmail, req.contactPhone, req.website, addr, req.groupName, req.approver,
                req.adminFirstName, req.adminLastName, req.adminEmail, req.adminMobile);
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
    @PreAuthorize("hasAnyRole('BUSINESS_ADMIN','HR_ADMIN','ADMIN')")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        return service.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/test")
    public String secureTest() {
        return " b2bunit working!";
    }
    public static class SelfOnboardRequest {
        @NotBlank public String name;
        @NotNull public B2BUnitType type;
        @Email public String contactEmail;
        public String contactPhone;
        public String website;
        public AddressDTO address;
        public String groupName; // optional: associate to a group by name
    }

    public static class AdminOnboardRequest extends SelfOnboardRequest {
        public String approver;
        public String adminFirstName;
        public String adminLastName;
        @Email public String adminEmail;
        public String adminMobile;
    }

    public static class AddressDTO {
        public String line1;
        public String line2;
        public String city;
        public String state;
        public String country;
        public String postalCode;
        public String fullText; // optional: server may derive this if not provided
    }
}
