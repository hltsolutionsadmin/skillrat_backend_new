package com.skillrat.user.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillrat.common.dto.UserDTO;
import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.repo.UserRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String currentTenant() {
        return Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
    }

    private void setB2BUnitIfPresent(User u, UUID b2bUnitId) {
        if (b2bUnitId != null) {
            com.skillrat.user.organisation.domain.B2BUnit bu = new com.skillrat.user.organisation.domain.B2BUnit();
            bu.setId(b2bUnitId);
            u.setB2bUnit(bu);
        }
    }

    private Role ensureRole(String name, String description, UUID b2bUnitId) {
        // First try to find the role by name and b2bUnitId
        Optional<Role> existingRole = roleRepository.findByNameAndB2bUnitId(name, b2bUnitId);
        if (existingRole.isPresent()) {
            return existingRole.get();
        }
        
        // If not found, check if a role with the same name exists (regardless of b2bUnitId)
        Optional<Role> roleWithSameName = roleRepository.findByName(name);
        if (roleWithSameName.isPresent()) {
            // A role with this name already exists, but with a different b2bUnitId
            // We need to create a new role with a different name
            String newName = name + "_" + (b2bUnitId != null ? b2bUnitId.toString().substring(0, 8) : "GLOBAL");
            log.warn("Role with name '{}' already exists with different b2bUnitId. Creating new role with name: {}", 
                    name, newName);
            Role newRole = new Role(newName, description, b2bUnitId);
            return roleRepository.save(newRole);
        }
        
        // No role with this name exists, create a new one
        Role role = new Role(name, description, b2bUnitId);
        return roleRepository.save(role);
    }

    @Transactional
    public User signup(com.skillrat.user.web.dto.SignupRequest req) {
        String tenantId = currentTenant();

        // Create and save user
        User user = new User();
        user.setFirstName(req.firstName != null ? req.firstName.trim() : null);
        user.setLastName(req.lastName != null ? req.lastName.trim() : null);
        user.setUsername(req.email.toLowerCase().trim());
        user.setEmail(req.email.toLowerCase().trim());
        user.setMobile(req.mobile != null ? req.mobile.trim() : null);
        user.setPasswordHash(passwordEncoder.encode(req.password));
        user.setActive(true);
        user.setTenantId(tenantId);
        
        // Assign default ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        user.setRoles(Set.of(userRole));
        
        User saved = userRepository.save(user);
        log.info("User signup successful id={}, email={}, tenantId={}", saved.getId(), saved.getEmail(), tenantId);
        return saved;
    }

    @Transactional
    public User adminCreateUser(UUID b2bUnitId, String firstName, String lastName, String email, String mobile, List<UUID> roleIds) {
        String tenantId = currentTenant();
        User u = new User();
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setUsername(email.toLowerCase());
        u.setEmail(email.toLowerCase());
        u.setMobile(mobile);
        u.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        u.setActive(true);
        u.setTenantId(tenantId);
        setB2BUnitIfPresent(u, b2bUnitId);
        u.setPasswordNeedsReset(true);
        u.setPasswordSetupToken(UUID.randomUUID().toString());
        u.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));
        Role r = ensureRole("BUSINESS_ADMIN", "Business Admin", b2bUnitId);
        Set<Role> roles = Optional.ofNullable(u.getRoles()).orElseGet(HashSet::new);
        roles.add(r);
        u.setRoles(roles);
        return userRepository.save(u);
    }

    @Transactional
    public User adminUpdateUser(UUID id, String firstName, String lastName, String mobile, Boolean active, List<UUID> roleIds) {
        if (id == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
            
        // Update basic info
        if (firstName != null) {
            user.setFirstName(firstName.isBlank() ? null : firstName.trim());
        }
        
        if (lastName != null) {
            user.setLastName(lastName.isBlank() ? null : lastName.trim());
        }
        
        // Update mobile with validation
        if (mobile != null) {
            String trimmedMobile = mobile.isBlank() ? null : mobile.trim();
            if (trimmedMobile != null && !trimmedMobile.equals(user.getMobile())) {
                userRepository.findByMobile(trimmedMobile)
                    .filter(x -> !x.getId().equals(id))
                    .ifPresent(x -> { 
                        throw new IllegalArgumentException("Mobile number already in use"); 
                    });
                user.setMobile(trimmedMobile);
            }
        }
        
        // Update active status
        if (active != null) {
            user.setActive(active);
        }
        
        // Update roles if provided
        if (roleIds != null) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            
            // Ensure the user always has at least the ROLE_USER
            if (roles.stream().noneMatch(role -> "ROLE_USER".equals(role.getName()))) {
                Role userRole = getOrCreateRole("ROLE_USER", "Default role for all users", (user.getB2bUnit() != null ? user.getB2bUnit().getId() : null));
                roles.add(userRole);
            }
            
            user.setRoles(roles);
        }
        
        // Update audit fields
        user.setUpdatedDate(Instant.now());
        
        // Get current user for audit
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();
            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                user.setUpdatedBy(email);
            }
        }
        
        User updatedUser = userRepository.save(user);
        log.info("Admin updated user id={}, email={}", updatedUser.getId(), updatedUser.getEmail());
        return updatedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(String emailOrMobile, String rawPassword) {
        // First try to find by email
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(emailOrMobile);
        
        // If not found by email, try by mobile
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByMobile(emailOrMobile);
        }
        
        // Check if user exists, is active, and password matches
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isActive()) {
                log.warn("Authentication failed: User {} is inactive", emailOrMobile);
                return Optional.empty();
            }
            
            if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
                log.warn("Authentication failed: Invalid password for user {}", emailOrMobile);
                return Optional.empty();
            }
            
            log.info("User authenticated successfully id={}, email={}", user.getId(), user.getEmail());
            return Optional.of(user);
        }
        
        log.warn("Authentication failed: User not found with identifier={}", emailOrMobile);
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        userOpt.ifPresentOrElse(
            user -> log.debug("Found user by email: {}", email),
            () -> log.debug("No user found with email: {}", email)
        );
        return userOpt;
    }

    @Transactional(readOnly = true)
    public Optional<User> getById(UUID id) {
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresentOrElse(
            user -> log.debug("Found user by id: {}", id),
            () -> log.debug("No user found with id: {}", id)
        );
        return userOpt;
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsers(UUID b2bUnitId, String q, String role, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        String roleName = (role == null || role.isBlank() || "All".equalsIgnoreCase(role)) ? null : role.trim();
       return userRepository.search(b2bUnitId, query, "BUSINESS_ADMIN", pageable);
    }

    @Transactional
    public User createBusinessAdmin(UUID b2bUnitId, String firstName, String lastName, String email, String mobile) {
        String tenantId = currentTenant();
        
        // Create new user
        User admin = new User();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setUsername(email.toLowerCase());
        admin.setEmail(email.toLowerCase());
        admin.setMobile(mobile);
        admin.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        admin.setActive(true);
        admin.setTenantId(tenantId);
        setB2BUnitIfPresent(admin, b2bUnitId);
        admin.setPasswordNeedsReset(true);
        admin.setPasswordSetupToken(UUID.randomUUID().toString());
        admin.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));

        // Ensure ROLE_ADMIN exists for this business
        Role adminRole = ensureRole("ROLE_ADMIN", "Business Administrator", b2bUnitId);
            
        // Assign roles
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        
        // Ensure user also has ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        roles.add(userRole);
        
        admin.setRoles(roles);
        User saved = userRepository.save(admin);
        
        log.info("Business admin user created id={}, email={}, b2bUnitId={}, tenantId={}",
            saved.getId(), saved.getEmail(), b2bUnitId, tenantId);
            
        return saved;
    }

    @Transactional
    public User assignBusinessAdmin(UUID b2bUnitId, String email) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        
        // Find the user by email
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
            
        log.debug("Assigning BUSINESS_ADMIN role to user: {}", email);

        // Ensure ROLE_BUSINESS_ADMIN exists for this business
        Role businessAdminRole = ensureRole("ROLE_BUSINESS_ADMIN", "Business Administrator", b2bUnitId);

        // Get existing roles or create a new set if none exists
        Set<Role> roles = new HashSet<>(user.getRoles());
            
        // Add the business admin role
        roles.add(businessAdminRole);
        
        // Ensure the user has the ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        roles.add(userRole);
        
        // Update user roles and business unit
        user.setRoles(roles);
        setB2BUnitIfPresent(user, b2bUnitId);
        
        // Save the updated user
        User saved = userRepository.save(user);
        
        log.info("Assigned ROLE_BUSINESS_ADMIN to user id={}, email={}, b2bUnitId={}, tenantId={}",
            saved.getId(), saved.getEmail(), b2bUnitId, tenantId);
            
        return saved;
    }

    @Transactional
    public Employee inviteEmployee(UUID b2bUnitId, com.skillrat.user.web.dto.InviteEmployeeRequest req) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        
        // Check if email already exists
        userRepository.findByEmailIgnoreCase(req.email).ifPresent(u -> { 
            throw new IllegalArgumentException("Email already in use"); 
        });
        
        // Create new employee
        Employee emp = new Employee();
        emp.setFirstName(req.firstName);
        emp.setLastName(req.lastName);
        emp.setUsername(req.email.toLowerCase());
        emp.setEmail(req.email.toLowerCase());
        emp.setMobile(req.mobile);
        emp.setEmployeeCode("EMP-" + UUID.randomUUID().toString().substring(0, 8));
        emp.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        emp.setActive(true);
        emp.setTenantId(tenantId);
        setB2BUnitIfPresent(emp, b2bUnitId);
        emp.setPasswordNeedsReset(true);
        emp.setPasswordSetupToken(UUID.randomUUID().toString());
        emp.setPasswordSetupTokenExpires(Instant.now().plus(7, ChronoUnit.DAYS));
        
        // Handle role assignments
        Set<Role> roles = new HashSet<>();
        
        // Add specified roles if any
        if (req.roleIds != null && !req.roleIds.isEmpty()) {
            Set<Role> specifiedRoles = new HashSet<>(roleRepository.findAllById(req.roleIds));
            roles.addAll(specifiedRoles);
        }
        
        // Ensure the user has at least the ROLE_EMPLOYEE
        Role employeeRole = roleRepository.findByNameAndB2bUnitId("ROLE_EMPLOYEE", b2bUnitId)
            .orElseGet(() -> {
                Role role = new Role("ROLE_EMPLOYEE", "Employee role with basic access", b2bUnitId);
                return roleRepository.save(role);
            });
        roles.add(employeeRole);
        
        // Ensure the user has the ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        roles.add(userRole);
        
        emp.setRoles(roles);
        
        // Populate createdBy/updatedBy from current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String actor = auth.getName();
            Object principal = auth.getPrincipal();
            if (principal instanceof Jwt) {
                Jwt jwt = (Jwt) principal;
                String emailClaim = jwt.getClaimAsString("email");
                if (emailClaim != null && !emailClaim.isBlank()) {
                    actor = emailClaim;
                }
            }
            if (actor != null && !actor.isBlank()) {
                emp.setCreatedBy(actor);
                emp.setUpdatedBy(actor);
            }
        }
        
        // Save the employee
        Employee savedEmployee = userRepository.save(emp);
        
        log.info("Employee invited successfully id={}, email={}, employeeCode={}, b2bUnitId={}, tenantId={}",
            savedEmployee.getId(), 
            savedEmployee.getEmail(), 
            savedEmployee.getEmployeeCode(),
            b2bUnitId, 
            tenantId);
            
        // TODO: Send invitation email with setup token
        
        return savedEmployee;
    }

    /**
     * Helper method to get or create a role if it doesn't exist
     */
    private Role getOrCreateRole(String roleName, String description, UUID b2bUnitId) {
        return roleRepository.findByNameAndB2bUnitId(roleName, b2bUnitId)
            .orElseGet(() -> {
                Role role = new Role(roleName, description, b2bUnitId);
                return roleRepository.save(role);
            });
    }
    
    /**
     * Get default roles for a business unit
     */
    private Set<Role> getDefaultRoles(UUID b2bUnitId) {
        Set<Role> roles = new HashSet<>();
        
        // Get or create ROLE_EMPLOYEE for the specific business unit
        Role employeeRole = roleRepository.findByNameAndB2bUnitId("ROLE_EMPLOYEE", b2bUnitId)
            .orElseGet(() -> {
                Role role = new Role("ROLE_EMPLOYEE", "Employee role with basic access", b2bUnitId);
                return roleRepository.save(role);
            });
        roles.add(employeeRole);
        
        // Get global ROLE_USER (must be pre-seeded by initializer)
        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new IllegalStateException("Required role ROLE_USER is not initialized"));
        roles.add(userRole);
        
        return roles;
    }

    @Transactional
    public boolean setupPassword(String token, String newPassword) {
        // Validate input parameters
        if (token == null || token.isBlank()) {
            log.warn("Password setup failed: Token is required");
            return false;
        }
        if (newPassword == null || newPassword.length() < 8) {
            log.warn("Password setup failed: New password must be at least 8 characters long");
            return false;
        }
        
        // Find user by token
        Optional<User> userOpt = userRepository.findByPasswordSetupToken(token);
        if (userOpt.isEmpty()) { log.warn("Password setup failed: Invalid or expired token"); return false; }
        User user = userOpt.get();
        
        // Check if token is expired
        if (user.getPasswordSetupTokenExpires() == null || user.getPasswordSetupTokenExpires().isBefore(Instant.now())) {
            log.warn("Password setup failed: Token expired for user id={}, email={}", user.getId(), user.getEmail());
            return false;
        }
        
        // Update user password and clear reset flags
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordNeedsReset(false);
        user.setPasswordSetupToken(null);
        user.setPasswordSetupTokenExpires(null);
        
        // Update updatedBy if there's an authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String actor = auth.getName();
            if (actor != null && !actor.isBlank()) {
                user.setUpdatedBy(actor);
            }
        }
        
        // Save the updated user
        userRepository.save(user);
        
        log.info("Password setup completed successfully for user id={}, email={}", 
            user.getId(), user.getEmail());
            
        // TODO: Send confirmation email to the user
        
        return true;
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID userId) throws Exception {
        UserDTO userDTO=new UserDTO();
        Optional<User> user=userRepository.findById(userId);
        if(!user.isPresent()){
          throw new Exception("User not found with id: " + userId);  
        }
        populateUser(userDTO,user.get());
        return userDTO;
    }

    private void populateUser(UserDTO userDTO, User user) {
        userDTO.setId(user.getId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getUsersByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyList();
        return userRepository.findAllById(ids).stream().filter(java.util.Objects::nonNull).map(u -> {
            UserDTO dto = new UserDTO();
            populateUser(dto, u);
            return dto;
        }).toList();
    }
}
