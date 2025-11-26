package com.skillrat.user.repo;

import com.skillrat.user.domain.CoinCategory;
import com.skillrat.user.domain.UserCoinCategoryProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCoinCategoryProgressRepository extends JpaRepository<UserCoinCategoryProgress, UUID> {
    Optional<UserCoinCategoryProgress> findByUserIdAndCategory(UUID userId, CoinCategory category);
}
