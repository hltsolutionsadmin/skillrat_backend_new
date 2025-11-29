package com.skillrat.user.organisation.web;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.service.B2BUnitService;
import com.skillrat.user.organisation.web.dto.OnboardRequest;
import com.skillrat.user.organisation.web.dto.B2BUnitDTO;
import com.skillrat.user.organisation.web.mapper.B2BUnitMapper;

@RestController
@RequestMapping("/api/b2b")
@Validated
public class B2BUnitController {

    private final B2BUnitService service;

    public B2BUnitController(B2BUnitService service) { this.service = service; }

    @PostMapping("/onboard/self")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<B2BUnitDTO> selfOnboard(@RequestBody @Validated OnboardRequest request) {
        B2BUnit unit = service.selfSignup(request);
        return ResponseEntity.ok(B2BUnitMapper.toDTO(unit));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<B2BUnitDTO> approve(@PathVariable("id") @NonNull UUID id, @RequestBody Map<String, String> body) {
        String approver = body != null ? body.getOrDefault("approver", "skillrat-admin") : "skillrat-admin";
        return service.approve(id, approver)
                .<ResponseEntity<B2BUnitDTO>>map(u -> ResponseEntity.ok(B2BUnitMapper.toDTO(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<B2BUnitDTO> pending(Pageable pageable) {
        Page<B2BUnit> page = service.listPending(pageable);
        return page.map(B2BUnitMapper::toDTOList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<B2BUnitDTO> getById(@PathVariable("id") @NonNull UUID id) {
        return service.findById(id)
                .<ResponseEntity<B2BUnitDTO>>map(u -> ResponseEntity.ok(B2BUnitMapper.toDTO(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
