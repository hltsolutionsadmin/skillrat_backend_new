package com.skillrat.project.repo;

import com.skillrat.project.domain.HolidayCalendar;
import com.skillrat.project.domain.IndiaCity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface HolidayCalendarRepository extends JpaRepository<HolidayCalendar, UUID> {
    Optional<HolidayCalendar> findByNameAndTenantId(String name, String tenantId);
    Optional<HolidayCalendar> findByCodeAndTenantId(String code, String tenantId);

    interface HolidayCalendarSummary {
        UUID getId();
        String getCode();
        String getName();
        IndiaCity getCity();
        Long getHolidayCount();
        String getSampleProjectName();
    }

    @Query(value = """
            select c.id as id,
                   c.code as code,
                   c.name as name,
                   c.city as city,
                   count(d.id) as holidayCount,
                   '' as sampleProjectName
            from HolidayCalendar c
            left join c.days d
            where c.tenantId = :tenant
              and (:city is null or c.city = :city)
              and (:q is null or lower(c.code) like lower(concat('%', :q, '%'))
                              or lower(c.name) like lower(concat('%', :q, '%')))
            group by c.id, c.code, c.name, c.city
            """,
            countQuery = """
            select count(distinct c.id)
            from HolidayCalendar c
            where c.tenantId = :tenant
              and (:city is null or c.city = :city)
              and (:q is null or lower(c.code) like lower(concat('%', :q, '%'))
                              or lower(c.name) like lower(concat('%', :q, '%')))
            """)
    Page<HolidayCalendarSummary> searchCalendars(@Param("tenant") String tenant,
                                                 @Param("q") String q,
                                                 @Param("city") IndiaCity city,
                                                 Pageable pageable);
}
