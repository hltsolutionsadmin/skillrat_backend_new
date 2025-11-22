package com.skillrat.user.service;

import com.skillrat.common.exception.ResourceNotFoundException;
import com.skillrat.user.domain.BusinessUserRole;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.BusinessUserRoleRepository;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing business-specific role assignments.
 */
@Service
@RequiredArgsConstructor
public class BusinessRoleService {
    private static final Logger log = LoggerFactory.getLogger(BusinessRoleService.class);
    
    private final BusinessUserRoleRepository businessUserRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    
    /**
     * Assign a role to a user in a specific business
     */
    @Transactional
    public BusinessUserRole assignRoleToUser(UUID userId, UUID businessId, String roleName, String assignedBy) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
            
        // Check if the role is already assigned
        if (businessUserRoleRepository.existsByUserAndBusinessIdAndRole(user, businessId, role)) {
            throw new IllegalStateException("User already has the role " + roleName + " in this business");
        }
        
        BusinessUserRole businessUserRole = new BusinessUserRole();
        businessUserRole.setUser(user);
        businessUserRole.setBusinessId(businessId);
        businessUserRole.setRole(role);
        businessUserRole.setAssignedBy(assignedBy);
        
        return businessUserRoleRepository.save(businessUserRole);
    }
    
    /**
     * Remove a role from a user in a specific business
     */
    @Transactional
    public void removeRoleFromUser(UUID userId, UUID businessId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + roleName));
            
        if (!businessUserRoleRepository.existsByUserAndBusinessIdAndRole(user, businessId, role)) {
            throw new IllegalStateException("User does not have the role " + roleName + " in this business");
        }
        
        businessUserRoleRepository.deleteByUserAndBusinessIdAndRole(user, businessId, role);
    }
    
    /**
     * Get all roles for a user in a specific business
     */
    @Transactional(readOnly = true)
    public List<Role> getUserRolesInBusiness(UUID userId, UUID businessId) {
        return businessUserRoleRepository.findRolesByUserAndBusinessId(userId, businessId);
    }
    
    /**
     * Get all users with a specific role in a business
     */
    @Transactional(readOnly = true)
    public List<User> getUsersByRoleInBusiness(UUID businessId, String roleName) {
        return businessUserRoleRepository.findUsersByBusinessAndRoleName(businessId, roleName);
    }
    
    /**
     * Check if a user has a specific role in a business
     */
    @Transactional(readOnly = true)
    public boolean hasRoleInBusiness(UUID userId, UUID businessId, String roleName) {
        return businessUserRoleRepository.findRolesByUserAndBusinessId(userId, businessId).stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Check if a user has any of the specified roles in a business
     */
    @Transactional(readOnly = true)
    public boolean hasAnyRoleInBusiness(UUID userId, UUID businessId, List<String> roleNames) {
        Set<String> userRoles = businessUserRoleRepository.findRolesByUserAndBusinessId(userId, businessId).stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
            
        return roleNames.stream().anyMatch(userRoles::contains);
    }
    
    /**
     * Get all role assignments for a business
     */
    @Transactional(readOnly = true)
    public List<BusinessUserRole> getRoleAssignmentsForBusiness(UUID businessId) {
        return businessUserRoleRepository.findByBusinessId(businessId);
    }
    
    /**
     * Get all role assignments for a user across all businesses
     */
    @Transactional(readOnly = true)
    public List<BusinessUserRole> getRoleAssignmentsForUser(UUID userId) {
        return businessUserRoleRepository.findByUserId(userId);
    }
    
    /**
     * Remove all role assignments for a user in a business
     */
    @Transactional
    public void removeAllRolesFromUserInBusiness(UUID userId, UUID businessId) {
        businessUserRoleRepository.deleteByUserAndBusinessId(
            userRepository.getReferenceById(userId), 
            businessId
        );
    }
    
    /**
     * Remove all role assignments for a business
     */
    @Transactional
    public void removeAllRoleAssignmentsForBusiness(UUID businessId) {
        businessUserRoleRepository.deleteByBusinessId(businessId);
    }
    
    /**
     * Update a user's roles in a business (replaces all existing roles)
     */
    @Transactional
    public void updateUserRolesInBusiness(UUID userId, UUID businessId, List<String> roleNames, String assignedBy) {
        // Remove all existing roles
        removeAllRolesFromUserInBusiness(userId, businessId);
        
        // Add new roles
        for (String roleName : roleNames) {
            try {
                assignRoleToUser(userId, businessId, roleName, assignedBy);
            } catch (Exception e) {
                log.error("Failed to assign role {} to user {} in business {}: {}", 
                    roleName, userId, businessId, e.getMessage());
                throw e;
            }
        }
    }
}
