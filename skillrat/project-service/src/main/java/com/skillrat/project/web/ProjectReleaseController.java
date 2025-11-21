package com.skillrat.project.web;

import com.skillrat.project.domain.ProjectRelease;
import com.skillrat.project.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/releases")
@PreAuthorize("isAuthenticated()")
public class ProjectReleaseController {

    private final ProjectService projectService;

    public ProjectReleaseController(ProjectService projectService) {

        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectRelease> createRelease(
            @PathVariable UUID projectId,
            @RequestBody @Valid CreateReleaseRequest request) {
        ProjectRelease release = projectService.createProjectRelease(
                projectId,
                request.getVersion(),
                request.getStatus(),
                request.getProgress(),
                request.getStartDate(),
                request.getReleaseDate(),
                request.getDescription()
        );
        return ResponseEntity.ok(release);
    }

    @GetMapping
    public ResponseEntity<Page<ProjectRelease>> getReleases(
            @PathVariable UUID projectId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectReleases(projectId, pageable));
    }

    @GetMapping("/{releaseId}")
    public ResponseEntity<ProjectRelease> getRelease(
            @PathVariable UUID projectId,
            @PathVariable UUID releaseId) {
        return ResponseEntity.ok(projectService.getProjectRelease(projectId, releaseId));
    }

    @PutMapping("/{releaseId}")
    public ResponseEntity<ProjectRelease> updateRelease(
            @PathVariable UUID projectId,
            @PathVariable UUID releaseId,
            @RequestBody @Valid UpdateReleaseRequest request) {
        ProjectRelease updated = projectService.updateProjectRelease(
                projectId,
                releaseId,
                request.getVersion(),
                request.getStatus(),
                request.getProgress(),
                request.getStartDate(),
                request.getReleaseDate(),
                request.getDescription()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{releaseId}")
    public ResponseEntity<Void> deleteRelease(
            @PathVariable UUID projectId,
            @PathVariable UUID releaseId) {
        projectService.deleteProjectRelease(projectId, releaseId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{releaseId}/progress")
    public ResponseEntity<ProjectRelease> updateProgress(
            @PathVariable UUID projectId,
            @PathVariable UUID releaseId,
            @RequestParam @NotNull Integer progress) {
        return ResponseEntity.ok(projectService.updateReleaseProgress(projectId, releaseId, progress));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ProjectRelease>> getReleasesByStatus(
            @PathVariable UUID projectId,
            @PathVariable ProjectRelease.ReleaseStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(projectService.getProjectReleasesByStatus(projectId, status, pageable));
    }

    @Data
    public static class CreateReleaseRequest {
        @NotNull
        private String version;
        private ProjectRelease.ReleaseStatus status;
        private Integer progress;
        @NotNull
        private LocalDate startDate;
        @NotNull
        private LocalDate releaseDate;
        private String description;
    }

    @Data
    public static class UpdateReleaseRequest {
        private String version;
        private ProjectRelease.ReleaseStatus status;
        private Integer progress;
        private LocalDate startDate;
        private LocalDate releaseDate;
        private String description;
    }
}
