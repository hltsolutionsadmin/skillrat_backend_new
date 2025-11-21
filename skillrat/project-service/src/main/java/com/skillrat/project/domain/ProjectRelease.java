package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "project_releases")
@Getter
@Setter
@NoArgsConstructor
public class ProjectRelease extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReleaseStatus status = ReleaseStatus.PLANNED;

    @Column(nullable = false)
    private Integer progress = 0; // 0-100

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonBackReference("project-releases")
    private Project project;

    public enum ReleaseStatus {
        PLANNED,
        IN_PROGRESS,
        TESTING,
        DEPLOYED,
        DELAYED,
        CANCELLED
    }
}
