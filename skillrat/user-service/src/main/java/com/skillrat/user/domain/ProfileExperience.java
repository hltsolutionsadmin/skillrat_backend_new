package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profile_experiences")
@Getter
@Setter
@NoArgsConstructor
public class ProfileExperience extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExperienceType type; // PROJECT or INTERNSHIP

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(length = 200)
    private String organizationName; // company/college/team name

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "verifier_b2b_unit_id")
    private UUID verifierB2bUnitId; // business requested to verify

    @Column(name = "verified_by_user_id")
    private UUID verifiedByUserId;

    private Instant verifiedAt;
}
