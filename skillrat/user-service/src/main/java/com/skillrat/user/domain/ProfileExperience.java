package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a user's work experience, project, or internship in their profile.
 */
@Entity
@Table(name = "profile_experiences")
public class ProfileExperience extends BaseEntity {
    
    // Default constructor for JPA
    public ProfileExperience() {
    }
    
    // Constructor with required fields
    public ProfileExperience(ExperienceType type, String title, UUID userId) {
        this.type = type;
        this.title = title;
        this.userId = userId;
        this.verificationStatus = VerificationStatus.UNVERIFIED;
    }

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
    
    // Getters and Setters
    public ExperienceType getType() {
        return type;
    }
    
    public void setType(ExperienceType type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getOrganizationName() {
        return organizationName;
    }
    
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
    
    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
    
    public UUID getVerifierB2bUnitId() {
        return verifierB2bUnitId;
    }
    
    public void setVerifierB2bUnitId(UUID verifierB2bUnitId) {
        this.verifierB2bUnitId = verifierB2bUnitId;
    }
    
    public UUID getVerifiedByUserId() {
        return verifiedByUserId;
    }
    
    public void setVerifiedByUserId(UUID verifiedByUserId) {
        this.verifiedByUserId = verifiedByUserId;
    }
    
    public Instant getVerifiedAt() {
        return verifiedAt;
    }
    
    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileExperience that = (ProfileExperience) o;
        return Objects.equals(getId(), that.getId()) &&
               Objects.equals(userId, that.userId) &&
               type == that.type &&
               Objects.equals(title, that.title);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), userId, type, title);
    }
    
    @Override
    public String toString() {
        return "ProfileExperience{" +
                "id=" + getId() +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", userId=" + userId +
                ", verificationStatus=" + verificationStatus +
                '}';
    }
}
