package com.skillrat.user.repo;

import com.skillrat.user.domain.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    List<UserSkill> findByUserId(UUID userId);
}
