package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.util.ArrayList;
import java.util.List;

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
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private IncidentCategoryEntity category;

    @ManyToOne(optional = true)
    @JoinColumn(name = "sub_category_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private IncidentSubCategoryEntity subCategory;


    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MediaModel> media = new ArrayList<>();
    
    // Helper methods to manage bidirectional relationship
    public void addMedia(MediaModel media) {
        media.setIncident(this);
        this.media.add(media);
    }
    
    public void removeMedia(MediaModel media) {
        this.media.remove(media);
        media.setIncident(null);
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(name = "assignee_id")
    private java.util.UUID assigneeId;

    @Column(nullable = true, length = 200)
    private String assigneeName;

    @Column(name = "reporter_id")
    private java.util.UUID reporterId;

    @Column(nullable = true, length = 200)
    private String reporterName;
}
