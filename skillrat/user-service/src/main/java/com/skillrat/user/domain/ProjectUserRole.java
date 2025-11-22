package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.Objects;
import java.util.UUID;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectUserRole that = (ProjectUserRole) o;
        return Objects.equals(user != null ? user.getId() : null, that.user != null ? that.user.getId() : null) &&
               Objects.equals(projectId, that.projectId) &&
               Objects.equals(role != null ? role.getId() : null, that.role != null ? that.role.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            user != null ? user.getId() : null, 
            projectId, 
            role != null ? role.getId() : null
        );
    }

    // Builder pattern methods
    public static ProjectUserRoleBuilder builder() {
        return new ProjectUserRoleBuilder();
    }
    
    public static class ProjectUserRoleBuilder {
        private User user;
        private UUID projectId;
        private Role role;
        private String assignedBy;
        
        public ProjectUserRoleBuilder user(User user) {
            this.user = user;
            return this;
        }
        
        public ProjectUserRoleBuilder projectId(UUID projectId) {
            this.projectId = projectId;
            return this;
        }
        
        public ProjectUserRoleBuilder role(Role role) {
            this.role = role;
            return this;
        }
        
        public ProjectUserRoleBuilder assignedBy(String assignedBy) {
            this.assignedBy = assignedBy;
            return this;
        }
        
        public ProjectUserRole build() {
            ProjectUserRole projectUserRole = new ProjectUserRole();
            projectUserRole.setUser(this.user);
            projectUserRole.setProjectId(this.projectId);
            projectUserRole.setRole(this.role);
            projectUserRole.setAssignedBy(this.assignedBy);
            return projectUserRole;
        }
    }
}
