package com.skillrat.user.repo;

import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.EmploymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Query(value = """
            select e from Employee e
            left join e.reportingManager rm
            where e.b2bUnitId = :b2bUnitId
              and (
                    :q is null or lower(e.employeeCode) like lower(concat('%', :q, '%'))
                           or lower(e.firstName) like lower(concat('%', :q, '%'))
                           or lower(e.lastName) like lower(concat('%', :q, '%'))
                           or lower(e.email) like lower(concat('%', :q, '%'))
                           or lower(e.designation) like lower(concat('%', :q, '%'))
                           or lower(e.department) like lower(concat('%', :q, '%'))
                           or (rm is not null and lower(rm.firstName) like lower(concat('%', :q, '%')))
                  )
              and (:etype is null or e.employmentType = :etype)
            """,
            countQuery = """
            select count(e) from Employee e
            left join e.reportingManager rm
            where e.b2bUnitId = :b2bUnitId
              and (
                    :q is null or lower(e.employeeCode) like lower(concat('%', :q, '%'))
                           or lower(e.firstName) like lower(concat('%', :q, '%'))
                           or lower(e.lastName) like lower(concat('%', :q, '%'))
                           or lower(e.email) like lower(concat('%', :q, '%'))
                           or lower(e.designation) like lower(concat('%', :q, '%'))
                           or lower(e.department) like lower(concat('%', :q, '%'))
                           or (rm is not null and lower(rm.firstName) like lower(concat('%', :q, '%')))
                  )
              and (:etype is null or e.employmentType = :etype)
            """)
    Page<Employee> search(@Param("b2bUnitId") java.util.UUID b2bUnitId,
                          @Param("q") String q,
                          @Param("etype") EmploymentType employmentType,
                          Pageable pageable);
}
