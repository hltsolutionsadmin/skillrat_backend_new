package com.skillrat.user.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillrat.user.domain.ProjectUserRole;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.User;

@Repository
public interface ProjectUserRoleRepository extends JpaRepository<ProjectUserRole, UUID> {
    
    List<ProjectUserRole> findByUserAndProjectId(User user, UUID projectId);
    
    List<ProjectUserRole> findByUserId(UUID userId);
    
    List<ProjectUserRole> findByProjectId(UUID projectId);
    
    boolean existsByUserAndProjectIdAndRole(User user, UUID projectId, Role role);
    
    @Query("SELECT pur.role FROM ProjectUserRole pur WHERE pur.user.id = :userId AND pur.projectId = :projectId")
    List<Role> findRolesByUserAndProjectId(@Param("userId") UUID userId, @Param("projectId") UUID projectId);
    
    @Query("SELECT pur.user FROM ProjectUserRole pur WHERE pur.projectId = :projectId AND pur.role.name = :roleName")
    List<User> findUsersByProjectAndRoleName(@Param("projectId") UUID projectId, @Param("roleName") String roleName);
    
    void deleteByUserAndProjectIdAndRole(User user, UUID projectId, Role role);
    
    void deleteByProjectId(UUID projectId);
    
    /**
     * Deletes all project user roles for the given user and project ID.
     *
     * @param user the user
     * @param projectId the project ID
     */
    void deleteByUserAndProjectId(User user, UUID projectId);
}
