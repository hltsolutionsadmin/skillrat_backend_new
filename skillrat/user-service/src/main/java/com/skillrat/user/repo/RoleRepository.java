package com.skillrat.user.repo;

import com.skillrat.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    
    Optional<Role> findByName(String name);
    List<Role> findAllByName(String name);
    
    Optional<Role> findByNameAndB2bUnitId(String name, UUID b2bUnitId);
    
    List<Role> findByB2bUnitId(UUID b2bUnitId);
    
    List<Role> findByNameIn(List<String> roleNames);
    
    @Query("SELECT r FROM Role r WHERE r.b2bUnitId IS NULL OR r.b2bUnitId = :b2bUnitId")
    List<Role> findAvailableRolesForBusiness(@Param("b2bUnitId") UUID b2bUnitId);
    
    boolean existsByName(String name);
    
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId AND r.id IN :ids")
    List<Role> findByTenantIdAndIdIn(@Param("tenantId") String tenantId, @Param("ids") List<UUID> ids);
    
    /**
     * Find all roles that are not associated with any business unit (global roles).
     *
     * @return list of global roles
     */
    List<Role> findByB2bUnitIdIsNull();
}
