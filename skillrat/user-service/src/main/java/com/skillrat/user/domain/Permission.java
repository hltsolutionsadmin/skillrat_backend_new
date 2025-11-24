package com.skillrat.user.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a permission that can be assigned to roles.
 * Permissions define specific actions that can be performed in the system.
 */
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToMany(mappedBy = "permissions")
    @JsonManagedReference
    private Set<Role> roles = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return name != null && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Set<Role> getRoles() {
        if (roles == null) {
            roles = new HashSet<>();
        }
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    // Builder pattern
    public static PermissionBuilder builder() {
        return new PermissionBuilder();
    }
    
    public static class PermissionBuilder {
        private String name;
        private String description;
        private Set<Role> roles = new HashSet<>();
        
        public PermissionBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public PermissionBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public PermissionBuilder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }
        
        public Permission build() {
            Permission permission = new Permission();
            permission.setName(this.name);
            permission.setDescription(this.description);
            permission.setRoles(this.roles);
            return permission;
        }
    }
}
