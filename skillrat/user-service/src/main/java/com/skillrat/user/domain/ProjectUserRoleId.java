package com.skillrat.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ProjectUserRoleId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "project_id")
    private UUID projectId;
    
    @Column(name = "role_id")
    private UUID roleId;
    
    public ProjectUserRoleId() {}
    
    public ProjectUserRoleId(UUID userId, UUID projectId, UUID roleId) {
        this.userId = userId;
        this.projectId = projectId;
        this.roleId = roleId;
    }
    
    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getProjectId() {
        return projectId;
    }
    
    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }
    
    public UUID getRoleId() {
        return roleId;
    }
    
    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectUserRoleId that = (ProjectUserRoleId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(projectId, that.projectId) &&
               Objects.equals(roleId, that.roleId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, projectId, roleId);
    }
}
