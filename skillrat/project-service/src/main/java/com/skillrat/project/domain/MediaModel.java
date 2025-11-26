package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.util.Date;

@Entity
@Table(name = "media", indexes = {
        @Index(name = "idx_mediaid", columnList = "id", unique = true)
})
@Audited
@Getter
@Setter
public class MediaModel extends BaseEntity {

    @Column(name = "url", nullable = false)
    private String url;



    @Column(name = "file_name")
    private String fileName;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "description")
    private String description;

    @Column(name = "extension")
    private String extension;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "customer_id", length = 36)
    private String customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "creation_time", nullable = false)
    private Date creationTime;

    @Column(name = "modification_time")
    private Date modificationTime;

    @Column(name = "name")
    private String name;

    @PrePersist
    protected void onCreate() {
        this.creationTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modificationTime = new Date();
    }
}
