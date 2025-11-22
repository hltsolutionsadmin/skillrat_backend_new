package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents the association between a User, Business, and Role.
 * This allows users to have different roles in different businesses.
 */
@Setter
@Getter
@Entity
@Table(name = "business_user_roles",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user_id", "business_id", "role_id"}
       )
)
public class BusinessUserRole extends BaseEntity {

    // Getters and Setters
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "assigned_by", length = 255)
    private String assignedBy;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusinessUserRole that = (BusinessUserRole) o;
        return Objects.equals(user != null ? user.getId() : null, that.user != null ? that.user.getId() : null) &&
               Objects.equals(businessId, that.businessId) &&
               Objects.equals(role != null ? role.getId() : null, that.role != null ? that.role.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            user != null ? user.getId() : null, 
            businessId, 
            role != null ? role.getId() : null
        );
    }

    // Builder pattern methods
    public static BusinessUserRoleBuilder builder() {
        return new BusinessUserRoleBuilder();
    }
    
    public static class BusinessUserRoleBuilder {
        private User user;
        private UUID businessId;
        private Role role;
        private String assignedBy;
        
        public BusinessUserRoleBuilder user(User user) {
            this.user = user;
            return this;
        }
        
        public BusinessUserRoleBuilder businessId(UUID businessId) {
            this.businessId = businessId;
            return this;
        }
        
        public BusinessUserRoleBuilder role(Role role) {
            this.role = role;
            return this;
        }
        
        public BusinessUserRoleBuilder assignedBy(String assignedBy) {
            this.assignedBy = assignedBy;
            return this;
        }
        
        public BusinessUserRole build() {
            BusinessUserRole businessUserRole = new BusinessUserRole();
            businessUserRole.setUser(this.user);
            businessUserRole.setBusinessId(this.businessId);
            businessUserRole.setRole(this.role);
            businessUserRole.setAssignedBy(this.assignedBy);
            return businessUserRole;
        }
    }
}
