package com.skillrat.user.domain;

import java.util.UUID;

import com.skillrat.common.orm.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the association between a User, Project, and Role.
 * This allows users to have different roles in different projects.
 */
@Setter
@Getter
@Entity
@Table(name = "project_user_roles",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user_id", "project_id", "role_id"}
       )
)
public class ProjectUserRole extends BaseEntity {

    // Getters and Setters
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "assigned_by", length = 255)
    private String assignedBy;

}
