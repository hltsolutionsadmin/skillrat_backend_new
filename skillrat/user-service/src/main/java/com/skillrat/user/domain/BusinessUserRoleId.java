package com.skillrat.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BusinessUserRoleId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "business_id")
    private UUID businessId;
    
    @Column(name = "role_id")
    private UUID roleId;
    
    public BusinessUserRoleId() {}
    
    public BusinessUserRoleId(UUID userId, UUID businessId, UUID roleId) {
        this.userId = userId;
        this.businessId = businessId;
        this.roleId = roleId;
    }
    
    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public UUID getBusinessId() {
        return businessId;
    }
    
    public void setBusinessId(UUID businessId) {
        this.businessId = businessId;
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
        BusinessUserRoleId that = (BusinessUserRoleId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(businessId, that.businessId) &&
               Objects.equals(roleId, that.roleId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, businessId, roleId);
    }
}
