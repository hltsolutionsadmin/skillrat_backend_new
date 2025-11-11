package com.skillrat.wallet.repo;

import com.skillrat.wallet.domain.PointsCategory;
import com.skillrat.wallet.domain.WalletPointsLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletPointsLedgerRepository extends JpaRepository<WalletPointsLedger, UUID> {
    List<WalletPointsLedger> findByUserIdAndCategory(UUID userId, PointsCategory category);
}
