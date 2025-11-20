package com.skillrat.user.config;

import com.skillrat.user.domain.Permission;
import com.skillrat.user.domain.Role;
import com.skillrat.user.repo.PermissionRepository;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.service.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Component that initializes default roles and permissions in the system.
 * Runs automatically on application startup.
 */
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);
    

    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /**
     * Initialize default data on application startup
     */
    @PostConstruct
    @Transactional
    public void init() {
        log.info("Initializing default roles and permissions...");
        
        // Initialize default system roles
        roleService.initializeDefaultRoles();
        
        // Initialize default permissions
        initializeDefaultPermissions();
        
        // Assign permissions to roles
        assignPermissionsToRoles();
        
        log.info("Default roles and permissions initialized successfully");
    }
    
    /**
     * Initialize default system permissions
     */
    private void initializeDefaultPermissions() {
        // System permissions
        createPermissionIfNotExists("user:read", "View user information");
        createPermissionIfNotExists("user:create", "Create new users");
        createPermissionIfNotExists("user:update", "Update user information");
        createPermissionIfNotExists("user:delete", "Delete users");
        
        // Business permissions
        createPermissionIfNotExists("business:read", "View business information");
        createPermissionIfNotExists("business:create", "Create new businesses");
        createPermissionIfNotExists("business:update", "Update business information");
        createPermissionIfNotExists("business:delete", "Delete businesses");
        
        // Project permissions
        createPermissionIfNotExists("project:read", "View project information");
        createPermissionIfNotExists("project:create", "Create new projects");
        createPermissionIfNotExists("project:update", "Update project information");
        createPermissionIfNotExists("project:delete", "Delete projects");
        
        // Role management permissions
        createPermissionIfNotExists("role:read", "View roles and permissions");
        createPermissionIfNotExists("role:assign", "Assign roles to users");
        createPermissionIfNotExists("role:manage", "Create, update, and delete roles");
        
        // Other permissions
        createPermissionIfNotExists("settings:manage", "Manage system settings");
        createPermissionIfNotExists("reports:view", "View system reports");
    }
    
    /**
     * Assign permissions to default roles
     */
    private void assignPermissionsToRoles() {
        // Admin role gets all permissions
        assignPermissionsToRole(Role.ROLE_ADMIN, Arrays.asList(
            "user:read", "user:create", "user:update", "user:delete",
            "business:read", "business:create", "business:update", "business:delete",
            "project:read", "project:create", "project:update", "project:delete",
            "role:read", "role:assign", "role:manage",
            "settings:manage", "reports:view"
        ));
        
        // Business owner role gets business and project management permissions
        assignPermissionsToRole(Role.ROLE_BUSINESS_OWNER, Arrays.asList(
            "user:read", "user:create", "user:update",
            "business:read", "business:update",
            "project:read", "project:create", "project:update", "project:delete",
            "role:read", "role:assign",
            "reports:view"
        ));
        
        // Project manager role gets project management permissions
        assignPermissionsToRole(Role.ROLE_PROJECT_MANAGER, Arrays.asList(
            "user:read",
            "project:read", "project:update",
            "role:read"
        ));
        
        // Team lead role gets limited project permissions
        assignPermissionsToRole(Role.ROLE_TEAM_LEAD, Arrays.asList(
            "user:read",
            "project:read"
        ));
        
        // Regular user role gets basic read permissions
        assignPermissionsToRole(Role.ROLE_USER, Arrays.asList(
            "user:read",
            "project:read"
        ));
    }
    
    /**
     * Helper method to create a permission if it doesn't exist
     */
    private void createPermissionIfNotExists(String name, String description) {
        if (!permissionRepository.existsByName(name)) {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDescription(description);
            permissionRepository.save(permission);
            log.debug("Created permission: {}", name);
        }
    }
    
    /**
     * Helper method to assign permissions to a role
     */
    private void assignPermissionsToRole(String roleName, List<String> permissionNames) {
        roleRepository.findByName(roleName).ifPresent(role -> {
            Set<Permission> permissions = new HashSet<>();
            
            for (String permissionName : permissionNames) {
                permissionRepository.findByName(permissionName).ifPresent(permissions::add);
            }
            
            role.setPermissions(permissions);
            roleRepository.save(role);
            log.debug("Assigned {} permissions to role: {}", permissions.size(), roleName);
        });
    }
}
