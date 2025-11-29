package com.skillrat.user.repo;

import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.EmploymentType;
import com.skillrat.user.dto.DesignationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Query(value = """
            select e from Employee e
            left join e.reportingManager rm
            where e.id in (
                select u.id from User u 
                join u.b2bUnit bu 
                where bu.id = :b2bUnitId
            )
            and (
                  :q is null or lower(e.employeeCode) like lower(concat('%', :q, '%'))
                         or lower(e.firstName) like lower(concat('%', :q, '%'))
                         or lower(e.lastName) like lower(concat('%', :q, '%'))
                         or lower(e.email) like lower(concat('%', :q, '%'))
                         or (e.designation is not null and lower(e.designation.name) like lower(concat('%', :q, '%')))
                         or (e.department is not null and lower(e.department) like lower(concat('%', :q, '%')))
                         or (rm is not null and lower(rm.firstName) like lower(concat('%', :q, '%')))
                )
            and (:etype is null or e.employmentType = :etype)
            """,
            countQuery = """
            select count(e) from Employee e
            left join e.reportingManager rm
            where e.id in (
                select u.id from User u 
                join u.b2bUnit bu 
                where bu.id = :b2bUnitId
            )
            and (
                  :q is null or lower(e.employeeCode) like lower(concat('%', :q, '%'))
                         or lower(e.firstName) like lower(concat('%', :q, '%'))
                         or lower(e.lastName) like lower(concat('%', :q, '%'))
                         or lower(e.email) like lower(concat('%', :q, '%'))
                         or (e.designation is not null and lower(e.designation.name) like lower(concat('%', :q, '%')))
                         or (e.department is not null and lower(e.department) like lower(concat('%', :q, '%')))
                         or (rm is not null and lower(rm.firstName) like lower(concat('%', :q, '%')))
                )
            and (:etype is null or e.employmentType = :etype)
            """)
    Page<Employee> search(@Param("b2bUnitId") UUID b2bUnitId,
                         @Param("q") String q,
                         @Param("etype") EmploymentType employmentType,
                         Pageable pageable);

    List<Employee> findByB2bUnitId(UUID b2bUnitId);

    void deleteById(UUID userId);

    @Query("SELECT new com.skillrat.user.dto.DesignationDTO(" +
            "d.id, d.name, b.name, COUNT(e)) " +
            "FROM Employee e " +
            "JOIN e.designation d " +
            "LEFT JOIN e.band b " +      // allow null bands
            "WHERE d.b2bUnitId = :b2bUnitId " +  // moved filter to designation
            "GROUP BY d.id, d.name, b.name " +
            "ORDER BY d.name")
    List<DesignationDTO> findDesignationWithBandAndResourceCount(@Param("b2bUnitId") UUID b2bUnitId);
}


