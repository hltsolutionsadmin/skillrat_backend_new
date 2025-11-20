package com.skillrat.user.domain;

import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a role in the system that can be assigned to users.
 * Roles can be system-wide or specific to a business unit.
 */
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_BUSINESS_OWNER = "ROLE_BUSINESS_OWNER";
    public static final String ROLE_PROJECT_MANAGER = "ROLE_PROJECT_MANAGER";
    public static final String ROLE_TEAM_LEAD = "ROLE_TEAM_LEAD";
    public static final String ROLE_DEVELOPER = "ROLE_DEVELOPER";

    @Column(nullable = false, length = 64, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "b2b_unit_id")
    private UUID b2bUnitId;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
    // Constructors
    public Role() {
        // Default constructor
    }
    
    public Role(String name, String description, UUID b2bUnitId) {
        this.name = name;
        this.description = description;
        this.b2bUnitId = b2bUnitId;
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

    public UUID getB2bUnitId() {
        return b2bUnitId;
    }

    public void setB2bUnitId(UUID b2bUnitId) {
        this.b2bUnitId = b2bUnitId;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    // Helper methods for permission management
    public void addPermission(Permission permission) {
        if (permission != null) {
            if (this.permissions == null) {
                this.permissions = new HashSet<>();
            }
            this.permissions.add(permission);
            if (permission.getRoles() != null && !permission.getRoles().contains(this)) {
                permission.getRoles().add(this);
            }
        }
    }
    
    public void removePermission(Permission permission) {
        if (permission != null && this.permissions != null) {
            this.permissions.remove(permission);
            if (permission.getRoles() != null) {
                permission.getRoles().remove(this);
            }
        }
    }
    
    // Builder pattern methods
    public static RoleBuilder builder() {
        return new RoleBuilder();
    }

    public static class RoleBuilder {
        private UUID id;
        private String name;
        private String description;
        private UUID b2bUnitId;
        private Set<User> users = new HashSet<>();
        private Set<Permission> permissions = new HashSet<>();

        public RoleBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleBuilder description(String description) {
            this.description = description;
            return this;
        }

        public RoleBuilder b2bUnitId(UUID b2bUnitId) {
            this.b2bUnitId = b2bUnitId;
            return this;
        }

        public RoleBuilder users(Set<User> users) {
            this.users = users;
            return this;
        }

        public RoleBuilder permissions(Set<Permission> permissions) {
            this.permissions = permissions;
            return this;
        }

        public RoleBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public Role build() {
            Role role = new Role();
            if (id != null) role.setId(id);
            role.setName(name);
            role.setDescription(description);
            role.setB2bUnitId(b2bUnitId);
            if (users != null) role.setUsers(users);
            if (permissions != null) role.setPermissions(permissions);
            return role;
        }
    }

    // User management methods
    public void addUser(User user) {
        if (user != null) {
            if (this.users == null) {
                this.users = new HashSet<>();
            }
            this.users.add(user);
            if (user.getRoles() != null && !user.getRoles().contains(this)) {
                user.getRoles().add(this);
            }
        }
    }

    public void removeUser(User user) {
        if (user != null && this.users != null) {
            this.users.remove(user);
            if (user.getRoles() != null) {
                user.getRoles().remove(this);
            }
        }
    }

    // Object overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(getId(), role.getId()) &&
               Objects.equals(name, role.name) &&
               Objects.equals(b2bUnitId, role.b2bUnitId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), name, b2bUnitId);
    }
    
    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", b2bUnitId=" + b2bUnitId +
                '}';
    }
}
