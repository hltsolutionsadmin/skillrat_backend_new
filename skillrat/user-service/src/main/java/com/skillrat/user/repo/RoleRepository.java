package com.skillrat.user.repo;

import com.skillrat.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByNameAndB2bUnitId(String name, UUID b2bUnitId);
    List<Role> findByB2bUnitId(UUID b2bUnitId);
}
