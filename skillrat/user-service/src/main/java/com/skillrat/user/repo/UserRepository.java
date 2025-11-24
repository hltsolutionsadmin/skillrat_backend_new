package com.skillrat.user.repo;

import com.skillrat.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByMobile(String mobile);
    boolean existsByMobile(String mobile);
    Optional<User> findByUsername(String username);
    Optional<User> findByPasswordSetupToken(String token);

    @Query(value = """
        select distinct u from User u
        join u.roles r
        where (:q is null or
               lower(u.email) like lower(concat('%', :q, '%')) or
               lower(u.firstName) like lower(concat('%', :q, '%')) or
               lower(u.lastName) like lower(concat('%', :q, '%')) or
               lower(u.username) like lower(concat('%', :q, '%')))
          and (:role is null or r.name = :role)
          and (:b2bUnitId is null or r.b2bUnitId = :b2bUnitId)
        """,
            countQuery = """
        select count(distinct u.id) from User u
        join u.roles r
        where (:q is null or
               lower(u.email) like lower(concat('%', :q, '%')) or
               lower(u.firstName) like lower(concat('%', :q, '%')) or
               lower(u.lastName) like lower(concat('%', :q, '%')) or
               lower(u.username) like lower(concat('%', :q, '%')))
          and (:role is null or r.name = :role)
          and (:b2bUnitId is null or r.b2bUnitId = :b2bUnitId)
        """)
    Page<User> search(
            @Param("b2bUnitId") UUID b2bUnitId,
            @Param("q") String q,
            @Param("role") String role,
            Pageable pageable
    );

}
