package com.skillrat.user.organisation.web.mapper;

import com.skillrat.user.organisation.domain.Department;
import com.skillrat.user.organisation.web.dto.DepartmentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DepartmentMapper {
    DepartmentMapper INSTANCE = Mappers.getMapper(DepartmentMapper.class);

    @Mapping(target = "b2bUnitId", expression = "java(department.getB2bUnits() != null && !department.getB2bUnits().isEmpty() ? department.getB2bUnits().iterator().next().getId() : null)")
    DepartmentDTO toDto(Department department);

    @Mapping(target = "b2bUnits", ignore = true) // Will be handled in service
    Department toEntity(DepartmentDTO dto);
}
