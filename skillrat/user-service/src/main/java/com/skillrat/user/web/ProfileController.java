package com.skillrat.user.web;

import com.skillrat.user.domain.*;
import com.skillrat.user.service.ProfileService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
@Validated
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // Experiences (Project/Internship)
    @PostMapping("/experiences")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileExperience> addExperience(@RequestBody CreateExperienceRequest req, Authentication auth) {
        ProfileExperience e = profileService.addExperience(auth.getName(), req.type, req.title, req.description, req.organizationName, req.startDate, req.endDate);
        return ResponseEntity.ok(e);
    }

    @GetMapping("/experiences")
    @PreAuthorize("isAuthenticated()")
    public List<ProfileExperience> myExperiences(Authentication auth) {
        return profileService.myExperiences(auth.getName());
    }

    @PostMapping("/experiences/{id}/request-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> requestVerification(@PathVariable("id") UUID id, @RequestBody Map<String, String> body, Authentication auth) {
        UUID verifierB2b = body.get("verifierB2bUnitId") != null ? UUID.fromString(body.get("verifierB2bUnitId")) : null;
        return profileService.requestVerification(id, verifierB2b, auth.getName())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/experiences/{id}/verify")
    @PreAuthorize("hasAnyRole('HR_HEAD','HR_RECRUITER','HR_MANAGER','ADMIN')")
    public ResponseEntity<?> verifyExperience(@PathVariable("id") UUID id, @RequestParam("approve") boolean approve, Authentication auth) {
        return profileService.verifyExperience(id, approve, auth.getName())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Skills
    @PostMapping("/skills")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSkill> addSkill(@RequestBody CreateSkillRequest req, Authentication auth) {
        return ResponseEntity.ok(profileService.addSkill(auth.getName(), req.name, req.level));
    }

    @GetMapping("/skills")
    @PreAuthorize("isAuthenticated()")
    public List<UserSkill> mySkills(Authentication auth) { return profileService.mySkills(auth.getName()); }

    @DeleteMapping("/skills/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteSkill(@PathVariable("id") UUID id, Authentication auth) {
        profileService.deleteSkill(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // Education
    @PostMapping("/education")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Education> addEducation(@RequestBody CreateEducationRequest req, Authentication auth) {
        return ResponseEntity.ok(profileService.addEducation(auth.getName(), req.institution, req.degree, req.fieldOfStudy, req.startDate, req.endDate));
    }

    @GetMapping("/education")
    @PreAuthorize("isAuthenticated()")
    public List<Education> myEducation(Authentication auth) { return profileService.myEducation(auth.getName()); }

    // Titles
    @PostMapping("/titles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TitleRecord> addTitle(@RequestBody CreateTitleRequest req, Authentication auth) {
        return ResponseEntity.ok(profileService.addTitle(auth.getName(), req.title, req.startDate, req.endDate));
    }

    @GetMapping("/titles")
    @PreAuthorize("isAuthenticated()")
    public List<TitleRecord> myTitles(Authentication auth) { return profileService.myTitles(auth.getName()); }

    public static class CreateExperienceRequest {
        @NotNull public ExperienceType type; // PROJECT/INTERNSHIP
        @NotBlank public String title;
        public String description;
        public String organizationName;
        public LocalDate startDate;
        public LocalDate endDate;
    }

    public static class CreateSkillRequest {
        @NotBlank public String name;
        public String level;
    }

    public static class CreateEducationRequest {
        @NotBlank public String institution;
        @NotBlank public String degree;
        public String fieldOfStudy;
        public LocalDate startDate;
        public LocalDate endDate;
    }

    public static class CreateTitleRequest {
        @NotBlank public String title;
        public LocalDate startDate;
        public LocalDate endDate;
    }
}
