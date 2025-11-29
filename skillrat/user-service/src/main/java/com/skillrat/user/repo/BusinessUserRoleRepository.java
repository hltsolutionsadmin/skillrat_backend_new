package com.skillrat.user.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillrat.user.domain.BusinessUserRole;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;

@Repository
public interface BusinessUserRoleRepository extends JpaRepository<BusinessUserRole, UUID> {
    
    List<BusinessUserRole> findByUserAndBusinessId(User user, UUID businessId);
    
    List<BusinessUserRole> findByUserId(UUID userId);
    
    List<BusinessUserRole> findByBusinessId(UUID businessId);
    
    boolean existsByUserAndBusinessIdAndRole(User user, UUID businessId, Role role);
    
    @Query("SELECT bur.role FROM BusinessUserRole bur WHERE bur.user.id = :userId AND bur.businessId = :businessId")
    List<Role> findRolesByUserAndBusinessId(@Param("userId") UUID userId, @Param("businessId") UUID businessId);
    
    @Query("SELECT bur.user FROM BusinessUserRole bur WHERE bur.businessId = :businessId AND bur.role.name = :roleName")
    List<User> findUsersByBusinessAndRoleName(@Param("businessId") UUID businessId, @Param("roleName") String roleName);
    
    void deleteByUserAndBusinessIdAndRole(User user, UUID businessId, Role role);
    
    void deleteByBusinessId(UUID businessId);
    
    /**
     * Deletes all business user roles for the given user and business ID.
     *
     * @param user the user
     * @param businessId the business ID
     */
    void deleteByUserAndBusinessId(User user, UUID businessId);
}
