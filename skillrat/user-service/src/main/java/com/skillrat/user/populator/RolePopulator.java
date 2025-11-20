package com.skillrat.user.populator;

import com.skillrat.user.domain.Role;
import com.skillrat.user.dto.RoleDto;
import org.springframework.stereotype.Component;

@Component
public class RolePopulator {
    public RoleDto toDto(Role role) {
        if (role == null) return null;
        return new RoleDto(role.getId(), role.getName(), role.getB2bUnitId());
    }
}
