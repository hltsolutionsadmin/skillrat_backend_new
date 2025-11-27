package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import com.skillrat.user.organisation.domain.B2BUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a user's work experience, project, or internship in their profile.
 */
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @ManyToOne
    @JoinColumn(name = "verifier_b2b_unit_id")
    private B2BUnit verifierB2bUnit; // business requested to verify

    @ManyToOne
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedBy;

    private Instant verifiedAt;
}
