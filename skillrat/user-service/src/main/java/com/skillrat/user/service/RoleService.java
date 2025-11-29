package com.skillrat.user.service;

import com.skillrat.common.exception.ResourceNotFoundException;
import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.domain.Permission;
import com.skillrat.user.domain.Role;
import com.skillrat.user.repo.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing roles and role assignments.
 */
@Service
@RequiredArgsConstructor
public class RoleService {
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    private final RoleRepository roleRepository;
    @Value("${app.roles.init.enabled:true}")
    private boolean initRolesEnabled;
    
    // Default system roles
    public static final List<String> DEFAULT_ROLES = Arrays.asList(
        Role.ROLE_ADMIN,
        Role.ROLE_USER,
        Role.ROLE_BUSINESS_OWNER,
        Role.ROLE_PROJECT_MANAGER,
        Role.ROLE_TEAM_LEAD,
        Role.ROLE_DEVELOPER
    );

    /**
     * Initialize default system roles if they don't exist
     */
    @Transactional
    public void initializeDefaultRoles() {
        if (!initRolesEnabled) {
            log.info("Default role initialization is disabled via property 'app.roles.init.enabled=false'. Skipping.");
            return;
        }
        String tenantId = getCurrentTenantId();
        
        for (String roleName : DEFAULT_ROLES) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = new Role();
                role.setName(roleName);
                role.setDescription("System default role: " + roleName);
                role.setTenantId(tenantId);
                roleRepository.save(role);
                log.info("Created default role: {}", roleName);
            }
        }
    }

    /**
     * Create a new role
     */
    @Transactional
    public Role createRole(Role role) {
        String tenantId = getCurrentTenantId();
        
        if (roleRepository.existsByName(role.getName())) {
            throw new IllegalArgumentException("Role with name " + role.getName() + " already exists");
        }
        
        role.setTenantId(tenantId);
        return roleRepository.save(role);
    }
    
    /**
     * Create a new business-specific role
     */
    @Transactional
    public Role createBusinessRole(UUID b2bUnitId, String name, String description) {
        String tenantId = getCurrentTenantId();
        
        roleRepository.findByNameAndB2bUnitId(name, b2bUnitId)
            .ifPresent(r -> { 
                throw new IllegalArgumentException("Role " + name + " already exists for this business"); 
            });
            
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setB2bUnitId(b2bUnitId);
        role.setTenantId(tenantId);
        
        return roleRepository.save(role);
    }

    /**
     * Get role by ID
     */
    @Transactional(readOnly = true)
    public Role getRoleById(@NonNull UUID roleId) {
        return roleRepository.findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }
    
    /**
     * Get role by name
     */
    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
    }

    /**
     * List all roles for a specific business unit
     */
    @Transactional(readOnly = true)
    public List<Role> getRolesByBusiness(UUID b2bUnitId) {
        return roleRepository.findByB2bUnitId(b2bUnitId);
    }
    
    /**
     * List all system roles (not specific to any business)
     */
    @Transactional(readOnly = true)
    public List<Role> getSystemRoles(UUID b2bUnitId) {
        List<String> excludedRoles = List.of("ADMIN", "BUSINESS_ADMIN");
        return roleRepository.findByB2bUnitIdAndNameNotIn(b2bUnitId, excludedRoles);
    }
    
    /**
     * List all available roles for a business (both system and business-specific)
     */
    @Transactional(readOnly = true)
    public List<Role> getAvailableRolesForBusiness(UUID b2bUnitId) {
        return roleRepository.findAvailableRolesForBusiness(b2bUnitId);
    }
    
    /**
     * Get roles for a specific user
     */
    @Transactional(readOnly = true)
    public List<Role> getUserRoles(UUID userId) {
        return roleRepository.findRolesByUserId(userId);
    }
    
    /**
     * Update an existing role
     */
    @Transactional
    public Role updateRole(@NonNull UUID roleId, String name, String description) {
        Role role = getRoleById(roleId);
        
        if (DEFAULT_ROLES.contains(role.getName())) {
            throw new UnsupportedOperationException("Cannot modify system default roles");
        }
        
        role.setName(name);
        role.setDescription(description);
        
        return roleRepository.save(role);
    }
    
    /**
     * Delete a role
     */
    @Transactional
    public void deleteRole(@NonNull UUID roleId) {
        Role role = getRoleById(roleId);
        
        if (DEFAULT_ROLES.contains(role.getName())) {
            throw new UnsupportedOperationException("Cannot delete system default roles");
        }
        
        // Check if role is assigned to any users
        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException("Cannot delete role that is assigned to users");
        }
        
        roleRepository.delete(role);
    }
    
    /**
     * Check if a user has a specific role
     */
    @Transactional(readOnly = true)
    public boolean hasRole(UUID userId, String roleName) {
        return getUserRoles(userId).stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Check if a user has any of the specified roles
     */
    @Transactional(readOnly = true)
    public boolean hasAnyRole(UUID userId, List<String> roleNames) {
        Set<String> userRoles = getUserRoles(userId).stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
            
        return roleNames.stream().anyMatch(userRoles::contains);
    }
    
    /**
     * Get all permissions for a specific role
     */
    @Transactional(readOnly = true)
    public Set<String> getRolePermissions(String roleName) {
        return roleRepository.findByName(roleName)
            .map(role -> role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }
    
    private String getCurrentTenantId() {
        return Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
    }
}
