package com.skillrat.wallet.repo;

import com.skillrat.wallet.domain.WalletPoints;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletPointsRepository extends JpaRepository<WalletPoints, UUID> {
    Optional<WalletPoints> findByUserId(UUID userId);
}
