package com.skillrat.user.repo;

import com.skillrat.user.domain.ProfileExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileExperienceRepository extends JpaRepository<ProfileExperience, UUID> {
    List<ProfileExperience> findByUserId(UUID userId);
    Optional<ProfileExperience> findById(UUID id);

}
