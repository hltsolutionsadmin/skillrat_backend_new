package com.skillrat.user.service;

import com.skillrat.user.domain.Role;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    Role createRole(Role role);
    Role createRole(UUID b2bUnitId, String name);
    List<Role> list(UUID b2bUnitId);
    List<Role> listAll();
}
