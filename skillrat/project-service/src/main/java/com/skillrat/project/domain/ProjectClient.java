package com.skillrat.project.domain;

import com.skillrat.common.orm.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "project_client",
    indexes = {
        @Index(name = "idx_project_client_tenant", columnList = "tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class ProjectClient extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 64, unique = true)
    private String primaryContactEmail;
    
    @Column(length = 64, unique = true)
    private String secondaryContactEmail;
}
