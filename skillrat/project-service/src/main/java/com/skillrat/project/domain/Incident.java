package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "incident")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class Incident extends BaseEntity {
	
	@Column(nullable = false, length = 200)
    private String incidentNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentUrgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentImpact impact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private IncidentCategory category;

    @Column(length = 128)
    private String subCategory; // free-text or catalog-driven in future

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(name = "assignee_id")
    private java.util.UUID assigneeId;

    @Column(name = "reporter_id")
    private java.util.UUID reporterId;
}
