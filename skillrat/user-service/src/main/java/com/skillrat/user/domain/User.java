package com.skillrat.user.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.skillrat.common.orm.BaseEntity;
import com.skillrat.user.organisation.domain.B2BUnit;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base user entity that represents a user in the system.
 * This is the parent class for all user types.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@Getter
@Setter
@NoArgsConstructor
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

    @OneToOne
    @JoinColumn(name = "b2b_unit_id")
    private B2BUnit b2bUnit;
    
    public User(String username, String email, String passwordHash, String firstName, String lastName, boolean active) {
        this.username = username != null ? username.toLowerCase() : null;
        this.email = email != null ? email.toLowerCase() : null;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
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
}
