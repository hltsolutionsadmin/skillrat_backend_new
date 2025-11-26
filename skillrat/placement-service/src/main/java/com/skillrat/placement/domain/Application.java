package com.skillrat.placement.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
public class Application extends BaseEntity {

    @Column(name = "opening_id", nullable = false)
    private UUID openingId;

    @Column(nullable = false, length = 120)
    private String applicantName;

    @Column(nullable = false, length = 255)
    private String applicantEmail;

    @Column(length = 20)
    private String applicantPhone;

    @Column(length = 512)
    private String resumeUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "submitted_by_user_id")
    private UUID submittedByUserId; // if internal referral
}
