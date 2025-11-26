package com.skillrat.wallet.web;

import com.skillrat.wallet.domain.PointsCategory;
import com.skillrat.wallet.domain.WalletPoints;
import com.skillrat.wallet.repo.WalletPointsRepository;
import com.skillrat.wallet.service.WalletPointsService;
import com.skillrat.wallet.config.WalletPointsProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@Validated
public class WalletPointsController {

    private final WalletPointsService service;
    private final WalletPointsRepository walletRepo;
    private final WalletPointsProperties props;

    public WalletPointsController(WalletPointsService service, WalletPointsRepository walletRepo, WalletPointsProperties props) {
        this.service = service;
        this.walletRepo = walletRepo;
        this.props = props;
    }

    // Internal endpoint for cross-service awarding
    @PostMapping("/internal/award")
    public ResponseEntity<?> award(@RequestBody AwardRequest req) {
        PointsCategory cat = PointsCategory.valueOf(req.category);
        int credited = service.award(req.userId, cat, req.reason, req.relatedId);
        return ResponseEntity.ok(Map.of("credited", credited));
    }

    // GET variant for local testing
    @GetMapping("/internal/award")
    public ResponseEntity<?> awardGet(@RequestParam("userId") UUID userId,
                                      @RequestParam("category") String category,
                                      @RequestParam(value = "reason", required = false) String reason,
                                      @RequestParam(value = "relatedId", required = false) UUID relatedId) {
        PointsCategory cat = PointsCategory.valueOf(category);
        int credited = service.award(userId, cat, reason, relatedId);
        return ResponseEntity.ok(Map.of("credited", credited));
    }

    // Internal endpoint to check balance
    @GetMapping("/internal/balance")
    public ResponseEntity<?> balance(@RequestParam("userId") UUID userId) {
        WalletPoints w = walletRepo.findByUserId(userId).orElse(null);
        int balance = (w == null ? 0 : w.getBalance());
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    // Internal: view current config snapshot
    @GetMapping("/internal/config")
    public ResponseEntity<?> config() {
        Map<String, Object> cfg = Map.of(
                "education", Map.of("perAction", props.getEducation().getPerAction(), "maxPerCategory", props.getEducation().getMaxPerCategory()),
                "project", Map.of("perAction", props.getProject().getPerAction(), "maxPerCategory", props.getProject().getMaxPerCategory()),
                "internship", Map.of("perAction", props.getInternship().getPerAction(), "maxPerCategory", props.getInternship().getMaxPerCategory()),
                "skill", Map.of("perAction", props.getSkill().getPerAction(), "maxPerCategory", props.getSkill().getMaxPerCategory()),
                "title", Map.of("perAction", props.getTitle().getPerAction(), "maxPerCategory", props.getTitle().getMaxPerCategory())
        );
        return ResponseEntity.ok(cfg);
    }

    public static class AwardRequest {
        @NotNull public UUID userId;
        @NotBlank public String category; // EDUCATION, PROJECT, INTERNSHIP, SKILL, TITLE
        public String reason;
        public UUID relatedId;
    }
}
