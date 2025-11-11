package com.skillrat.user.service;

import com.skillrat.user.domain.*;
import com.skillrat.user.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final ProfileExperienceRepository experienceRepository;
    private final UserSkillRepository skillRepository;
    private final EducationRepository educationRepository;
    private final TitleRecordRepository titleRepository;
    private final WalletClient walletClient;

    public ProfileService(UserRepository userRepository,
                          ProfileExperienceRepository experienceRepository,
                          UserSkillRepository skillRepository,
                          EducationRepository educationRepository,
                          TitleRecordRepository titleRepository,
                          WalletClient walletClient) {
        this.userRepository = userRepository;
        this.experienceRepository = experienceRepository;
        this.skillRepository = skillRepository;
        this.educationRepository = educationRepository;
        this.titleRepository = titleRepository;
        this.walletClient = walletClient;
    }

    // Utilities
    private UUID currentUserIdByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UUID currentUserB2BUnitIdByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .map(User::getB2bUnitId)
                .orElse(null);
    }

    // Experiences
    @Transactional
    public ProfileExperience addExperience(String email, ExperienceType type, String title, String description, String orgName,
                                           java.time.LocalDate start, java.time.LocalDate end) {
        UUID userId = currentUserIdByEmail(email);
        ProfileExperience e = new ProfileExperience();
        e.setUserId(userId);
        e.setType(type);
        e.setTitle(title);
        e.setDescription(description);
        e.setOrganizationName(orgName);
        e.setStartDate(start);
        e.setEndDate(end);
        e.setVerificationStatus(VerificationStatus.UNVERIFIED);
        e = experienceRepository.save(e);
        // Award points based on experience type via wallet-service
        String cat = (type == ExperienceType.PROJECT) ? "PROJECT" : "INTERNSHIP";
        walletClient.award(userId, cat, type + " added", e.getId());
        return e;
    }

    @Transactional(readOnly = true)
    public List<ProfileExperience> myExperiences(String email) {
        UUID userId = currentUserIdByEmail(email);
        return experienceRepository.findByUserId(userId);
    }

    @Transactional
    public Optional<ProfileExperience> requestVerification(UUID expId, UUID verifierB2bUnitId, String email) {
        UUID userId = currentUserIdByEmail(email);
        return experienceRepository.findById(expId).map(e -> {
            if (!e.getUserId().equals(userId)) throw new IllegalArgumentException("Cannot request verification for other user's experience");
            e.setVerifierB2bUnitId(verifierB2bUnitId);
            e.setVerificationStatus(VerificationStatus.PENDING);
            return experienceRepository.save(e);
        });
    }

    @Transactional
    public Optional<ProfileExperience> verifyExperience(UUID expId, boolean approve, String verifierEmail) {
        UUID callerB2B = currentUserB2BUnitIdByEmail(verifierEmail);
        return experienceRepository.findById(expId).map(e -> {
            if (e.getVerifierB2bUnitId() == null || callerB2B == null || !e.getVerifierB2bUnitId().equals(callerB2B)) {
                throw new IllegalArgumentException("Not authorized to verify this experience");
            }
            e.setVerificationStatus(approve ? VerificationStatus.VERIFIED : VerificationStatus.REJECTED);
            e.setVerifiedAt(Instant.now());
            e.setVerifiedByUserId(currentUserIdByEmail(verifierEmail));
            return experienceRepository.save(e);
        });
    }

    // Skills
    @Transactional
    public UserSkill addSkill(String email, String name, String level) {
        UUID userId = currentUserIdByEmail(email);
        UserSkill s = new UserSkill();
        s.setUserId(userId);
        s.setName(name);
        s.setLevel(level);
        s = skillRepository.save(s);
        walletClient.award(userId, "SKILL", "Skill added", s.getId());
        return s;
    }

    @Transactional(readOnly = true)
    public List<UserSkill> mySkills(String email) {
        UUID userId = currentUserIdByEmail(email);
        return skillRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteSkill(String email, UUID skillId) {
        UUID userId = currentUserIdByEmail(email);
        skillRepository.findById(skillId).ifPresent(s -> {
            if (!s.getUserId().equals(userId)) throw new IllegalArgumentException("Cannot delete others' skill");
            skillRepository.delete(s);
        });
    }

    // Education
    @Transactional
    public Education addEducation(String email, String institution, String degree, String fieldOfStudy, java.time.LocalDate start, java.time.LocalDate end) {
        UUID userId = currentUserIdByEmail(email);
        Education ed = new Education();
        ed.setUserId(userId);
        ed.setInstitution(institution);
        ed.setDegree(degree);
        ed.setFieldOfStudy(fieldOfStudy);
        ed.setStartDate(start);
        ed.setEndDate(end);
        ed = educationRepository.save(ed);
        walletClient.award(userId, "EDUCATION", "Education added", ed.getId());
        return ed;
    }

    @Transactional(readOnly = true)
    public List<Education> myEducation(String email) {
        UUID userId = currentUserIdByEmail(email);
        return educationRepository.findByUserId(userId);
    }

    // Titles
    @Transactional
    public TitleRecord addTitle(String email, String title, java.time.LocalDate start, java.time.LocalDate end) {
        UUID userId = currentUserIdByEmail(email);
        TitleRecord t = new TitleRecord();
        t.setUserId(userId);
        t.setTitle(title);
        t.setStartDate(start);
        t.setEndDate(end);
        t = titleRepository.save(t);
        walletClient.award(userId, "TITLE", "Title added", t.getId());
        return t;
    }

    @Transactional(readOnly = true)
    public List<TitleRecord> myTitles(String email) {
        UUID userId = currentUserIdByEmail(email);
        return titleRepository.findByUserId(userId);
    }
}
