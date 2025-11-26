package com.skillrat.user.repo;




import com.skillrat.user.domain.EmployeeOrgBand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeBandRepository extends JpaRepository<EmployeeOrgBand, UUID> {

    List<EmployeeOrgBand> findByB2bUnitId(String b2bUnitId);
}
