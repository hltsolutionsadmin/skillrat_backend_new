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
    Optional<User> findByMobile(String mobile);
    Optional<User> findByUsername(String username);
    Optional<User> findByPasswordSetupToken(String token);

    @Query(value = """
            select u from User u
            left join u.roles r
            where (:q is null or lower(u.email) like lower(concat('%', :q, '%'))
                           or lower(u.firstName) like lower(concat('%', :q, '%'))
                           or lower(u.lastName) like lower(concat('%', :q, '%'))
                           or lower(u.username) like lower(concat('%', :q, '%')))
              and (:role is null or r.name = :role)
            group by u
            """,
            countQuery = """
            select count(distinct u.id) from User u
            left join u.roles r
            where (:q is null or lower(u.email) like lower(concat('%', :q, '%'))
                           or lower(u.firstName) like lower(concat('%', :q, '%'))
                           or lower(u.lastName) like lower(concat('%', :q, '%'))
                           or lower(u.username) like lower(concat('%', :q, '%')))
              and (:role is null or r.name = :role)
            """)
    Page<User> search(@Param("q") String q, @Param("role") String role, Pageable pageable);
}
