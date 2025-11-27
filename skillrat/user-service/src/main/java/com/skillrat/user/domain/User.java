package com.skillrat.user.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skillrat.common.orm.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Base user entity that represents a user in the system.
 * This is the parent class for all user types.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public class User extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(unique = true, length = 20)
    private String mobile;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    
    @Column(nullable = false)
    private boolean passwordNeedsReset = false;

    @Column(length = 120)
    private String passwordSetupToken;

    private Instant passwordSetupTokenExpires;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonManagedReference
    private Set<Role> roles = new HashSet<>();

    @Column(name = "b2b_unit_id")
    private UUID b2bUnitId;
    
    // Constructors
    public User() {
        // Default constructor for JPA
    }
    
    public User(String username, String email, String passwordHash, String firstName, String lastName, boolean active) {
        this.username = username != null ? username.toLowerCase() : null;
        this.email = email != null ? email.toLowerCase() : null;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username != null ? username.toLowerCase() : null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase() : null;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isPasswordNeedsReset() {
        return passwordNeedsReset;
    }

    public void setPasswordNeedsReset(boolean passwordNeedsReset) {
        this.passwordNeedsReset = passwordNeedsReset;
    }

    public String getPasswordSetupToken() {
        return passwordSetupToken;
    }

    public void setPasswordSetupToken(String passwordSetupToken) {
        this.passwordSetupToken = passwordSetupToken;
    }

    public Instant getPasswordSetupTokenExpires() {
        return passwordSetupTokenExpires;
    }

    public void setPasswordSetupTokenExpires(Instant passwordSetupTokenExpires) {
        this.passwordSetupTokenExpires = passwordSetupTokenExpires;
    }

    public Set<Role> getRoles() {
        return roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public UUID getB2bUnitId() {
        return b2bUnitId;
    }

    public void setB2bUnitId(UUID b2bUnitId) {
        this.b2bUnitId = b2bUnitId;
    }

    // Role management methods
    public void addRole(Role role) {
        if (role != null) {
            if (this.roles == null) {
                this.roles = new HashSet<>();
            }
            this.roles.add(role);
            if (role.getUsers() != null && !role.getUsers().contains(this)) {
                role.getUsers().add(this);
            }
        }
    }

    public void removeRole(Role role) {
        if (role != null && this.roles != null) {
            this.roles.remove(role);
            if (role.getUsers() != null) {
                role.getUsers().remove(this);
            }
        }
    }

    public boolean hasRole(String roleName) {
        if (this.roles == null || roleName == null) {
            return false;
        }
        return this.roles.stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }

    // Convenience methods for audit fields
    public void setUpdatedAt(Instant updatedAt) {
        super.setUpdatedDate(updatedAt);
    }

    public Instant getUpdatedAt() {
        return getUpdatedDate();
    }

    // Object overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId()) &&
               Objects.equals(username, user.username) &&
               Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), email, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", active=" + active +
                '}';
    }
}
