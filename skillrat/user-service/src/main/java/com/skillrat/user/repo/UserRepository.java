package com.skillrat.user.repo;

import com.skillrat.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByMobile(String mobile);
    Optional<User> findByUsername(String username);
    Optional<User> findByPasswordSetupToken(String token);
}
