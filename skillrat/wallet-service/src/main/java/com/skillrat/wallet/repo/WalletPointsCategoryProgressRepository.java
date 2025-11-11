package com.skillrat.wallet.repo;

import com.skillrat.wallet.domain.PointsCategory;
import com.skillrat.wallet.domain.WalletPointsCategoryProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletPointsCategoryProgressRepository extends JpaRepository<WalletPointsCategoryProgress, UUID> {
    Optional<WalletPointsCategoryProgress> findByUserIdAndCategory(UUID userId, PointsCategory category);
}
