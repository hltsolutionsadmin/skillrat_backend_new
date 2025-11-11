package com.skillrat.user.repo;

import com.skillrat.user.domain.UserCoins;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserCoinsRepository extends JpaRepository<UserCoins, UUID> {
    Optional<UserCoins> findByUserId(UUID userId);
}
