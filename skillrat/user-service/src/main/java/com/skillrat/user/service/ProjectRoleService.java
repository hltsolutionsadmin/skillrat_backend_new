package com.skillrat.user.service;

import com.skillrat.common.exception.ResourceNotFoundException;
import com.skillrat.user.domain.ProjectUserRole;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.ProjectUserRoleRepository;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing project-specific role assignments.
 */
@Service
@RequiredArgsConstructor
public class ProjectRoleService {
    private static final Logger log = LoggerFactory.getLogger(ProjectRoleService.class);
    
    private final ProjectUserRoleRepository projectUserRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusinessRoleService businessRoleService;
    
    /**
     * Assign a role to a user in a specific project
     */
    @Transactional
    public ProjectUserRole assignRoleToUser(@NonNull UUID userId, UUID projectId, String roleName, String assignedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
            
        // Check if the role is already assigned
        if (projectUserRoleRepository.existsByUserAndProjectIdAndRole(user, projectId, role)) {
            throw new IllegalStateException("User already has the role " + roleName + " in this project");
        }
        
        ProjectUserRole projectUserRole = new ProjectUserRole();
        projectUserRole.setUser(user);
        projectUserRole.setProjectId(projectId);
        projectUserRole.setRole(role);
        projectUserRole.setAssignedBy(assignedBy);
        
        return projectUserRoleRepository.save(projectUserRole);
    }
    
    /**
     * Remove a role from a user in a specific project
     */
    @Transactional
    public void removeRoleFromUser(@NonNull UUID userId, UUID projectId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
            
        if (!projectUserRoleRepository.existsByUserAndProjectIdAndRole(user, projectId, role)) {
            throw new IllegalStateException("User does not have the role " + roleName + " in this project");
        }
        
        projectUserRoleRepository.deleteByUserAndProjectIdAndRole(user, projectId, role);
    }
    
    /**
     * Get all roles for a user in a specific project
     */
    @Transactional(readOnly = true)
    public List<Role> getUserRolesInProject(UUID userId, UUID projectId) {
        return projectUserRoleRepository.findRolesByUserAndProjectId(userId, projectId);
    }
    
    /**
     * Get all users with a specific role in a project
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRoleInProject(UUID projectId, String roleName) {
        return projectUserRoleRepository.findUsersByProjectAndRoleName(projectId, roleName);
    }
    
    /**
     * Check if a user has a specific role in a project
     */
    @Transactional(readOnly = true)
    public boolean hasRoleInProject(UUID userId, UUID projectId, String roleName) {
        return projectUserRoleRepository.findRolesByUserAndProjectId(userId, projectId).stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Check if a user has any of the specified roles in a project
     */
    @Transactional(readOnly = true)
    public boolean hasAnyRoleInProject(UUID userId, UUID projectId, List<String> roleNames) {
        Set<String> userRoles = projectUserRoleRepository.findRolesByUserAndProjectId(userId, projectId).stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
            
        return roleNames.stream().anyMatch(userRoles::contains);
    }
    
    /**
     * Get all role assignments for a project
     */
    @Transactional(readOnly = true)
    public List<ProjectUserRole> getRoleAssignmentsForProject(UUID projectId) {
        return projectUserRoleRepository.findByProjectId(projectId);
    }
    
    /**
     * Get all role assignments for a user across all projects
     */
    @Transactional(readOnly = true)
    public List<ProjectUserRole> getRoleAssignmentsForUser(UUID userId) {
        return projectUserRoleRepository.findByUserId(userId);
    }
    
    /**
     * Remove all role assignments for a user in a project
     */
    @Transactional
    public void removeAllRolesFromUserInProject(@NonNull UUID userId, UUID projectId) {
        projectUserRoleRepository.deleteByUserAndProjectId(
            userRepository.getReferenceById(userId), 
            projectId
        );
    }
    
    /**
     * Remove all role assignments for a project
     */
    @Transactional
    public void removeAllRoleAssignmentsForProject(UUID projectId) {
        projectUserRoleRepository.deleteByProjectId(projectId);
    }
    
    /**
     * Update a user's roles in a project (replaces all existing roles)
     */
    @Transactional
    public void updateUserRolesInProject(@NonNull UUID userId, UUID projectId, List<String> roleNames, String assignedBy) {
        // Remove all existing roles
        removeAllRolesFromUserInProject(userId, projectId);
        
        // Add new roles
        for (String roleName : roleNames) {
            try {
                assignRoleToUser(userId, projectId, roleName, assignedBy);
            } catch (Exception e) {
                log.error("Failed to assign role {} to user {} in project {}: {}", 
                    roleName, userId, projectId, e.getMessage());
                throw e;
            }
        }
    }
    
    /**
     * Check if a user has a specific role in the business that owns the project
     */
    @Transactional(readOnly = true)
    public boolean hasRoleInProjectBusiness(UUID userId, UUID projectId, String roleName) {
        // In a real implementation, you would fetch the business ID associated with the project
        // For now, we'll assume there's a method to get the business ID from the project
        UUID businessId = getBusinessIdForProject(projectId);
        return businessRoleService.hasRoleInBusiness(userId, businessId, roleName);
    }
    
    // Helper method - should be implemented based on your project structure
    private UUID getBusinessIdForProject(UUID projectId) {
        // TODO: Implement this method to return the business ID for the given project
        // This is a placeholder implementation
        throw new UnsupportedOperationException("getBusinessIdForProject not implemented");
    }
}
