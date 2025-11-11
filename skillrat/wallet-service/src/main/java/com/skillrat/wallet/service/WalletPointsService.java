package com.skillrat.wallet.service;

import com.skillrat.wallet.config.WalletPointsProperties;
import com.skillrat.wallet.domain.PointsCategory;
import com.skillrat.wallet.domain.WalletPoints;
import com.skillrat.wallet.domain.WalletPointsCategoryProgress;
import com.skillrat.wallet.domain.WalletPointsLedger;
import com.skillrat.wallet.repo.WalletPointsCategoryProgressRepository;
import com.skillrat.wallet.repo.WalletPointsLedgerRepository;
import com.skillrat.wallet.repo.WalletPointsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WalletPointsService {
    private final WalletPointsProperties cfg;
    private final WalletPointsRepository walletRepo;
    private final WalletPointsLedgerRepository ledgerRepo;
    private final WalletPointsCategoryProgressRepository progressRepo;

    public WalletPointsService(WalletPointsProperties cfg,
                               WalletPointsRepository walletRepo,
                               WalletPointsLedgerRepository ledgerRepo,
                               WalletPointsCategoryProgressRepository progressRepo) {
        this.cfg = cfg;
        this.walletRepo = walletRepo;
        this.ledgerRepo = ledgerRepo;
        this.progressRepo = progressRepo;
    }

    private WalletPointsProperties.Category categoryConfig(PointsCategory category) {
        return switch (category) {
            case EDUCATION -> cfg.getEducation();
            case PROJECT -> cfg.getProject();
            case INTERNSHIP -> cfg.getInternship();
            case SKILL -> cfg.getSkill();
            case TITLE -> cfg.getTitle();
        };
    }

    @Transactional
    public int award(UUID userId, PointsCategory category, String reason, UUID relatedId) {
        WalletPointsProperties.Category c = categoryConfig(category);
        int perAction = c.getPerAction();
        int cap = c.getMaxPerCategory();
        if (perAction <= 0 || cap <= 0) return 0;

        WalletPointsCategoryProgress progress = progressRepo.findByUserIdAndCategory(userId, category)
                .orElseGet(() -> {
                    WalletPointsCategoryProgress p = new WalletPointsCategoryProgress();
                    p.setUserId(userId);
                    p.setCategory(category);
                    p.setEarned(0);
                    p.setActions(0);
                    return progressRepo.save(p);
                });
        int remaining = Math.max(0, cap - progress.getEarned());
        if (remaining <= 0) return 0;
        int credit = Math.min(perAction, remaining);

        WalletPoints wallet = walletRepo.findByUserId(userId).orElseGet(() -> {
            WalletPoints w = new WalletPoints();
            w.setUserId(userId);
            w.setBalance(0);
            return walletRepo.save(w);
        });
        wallet.setBalance(wallet.getBalance() + credit);
        walletRepo.save(wallet);

        WalletPointsLedger entry = new WalletPointsLedger();
        entry.setUserId(userId);
        entry.setCategory(category);
        entry.setDelta(credit);
        entry.setReason(reason);
        entry.setRelatedId(relatedId);
        ledgerRepo.save(entry);

        progress.setEarned(progress.getEarned() + credit);
        progress.setActions(progress.getActions() + 1);
        progressRepo.save(progress);
        return credit;
    }
}
