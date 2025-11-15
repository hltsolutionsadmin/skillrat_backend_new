package com.skillrat.user.repo;

import com.skillrat.user.domain.User;
import com.skillrat.user.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByMobile(String mobile);
    Optional<User> findByUsername(String username);
    Optional<User> findByPasswordSetupToken(String token);
    
    // Employee specific queries
    @Query("SELECT e FROM Employee e WHERE e.b2bUnitId = :b2bUnitId")
    Page<Employee> findByB2bUnitId(
        @Param("b2bUnitId") UUID b2bUnitId,
        Pageable pageable
    );
    
    @Query("SELECT e FROM Employee e WHERE e.b2bUnitId = :b2bUnitId AND " +
           "(LOWER(e.firstName) LIKE LOWER(:searchTerm) OR " +
           "LOWER(e.lastName) LIKE LOWER(:searchTerm) OR " +
           "LOWER(e.email) LIKE LOWER(:searchTerm) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(:searchTerm))")
    Page<Employee> findEmployeesByB2bUnitIdAndSearch(
        @Param("b2bUnitId") UUID b2bUnitId,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
    
    @Query("SELECT e FROM Employee e WHERE e.id = :id AND TYPE(e) = Employee")
    Optional<Employee> findEmployeeById(@Param("id") UUID id);
}
