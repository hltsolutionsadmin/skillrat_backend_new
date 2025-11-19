package com.skillrat.user.repo;

import com.skillrat.user.domain.SalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalaryComponentDao extends JpaRepository<SalaryComponent, UUID> {
    Optional<SalaryComponent> findByCodeIgnoreCase(String code);
}
