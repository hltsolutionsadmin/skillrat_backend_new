package com.skillrat.user.repo;

import com.skillrat.user.domain.CoinCategory;
import com.skillrat.user.domain.UserCoinLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserCoinLedgerRepository extends JpaRepository<UserCoinLedger, UUID> {
    List<UserCoinLedger> findByUserIdAndCategory(UUID userId, CoinCategory category);
}
