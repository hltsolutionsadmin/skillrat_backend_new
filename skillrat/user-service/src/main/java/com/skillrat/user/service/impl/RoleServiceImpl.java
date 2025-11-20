package com.skillrat.user.service.impl;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.user.domain.Role;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public Role createRole(Role role) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        role.setTenantId(tenantId);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role createRole(UUID b2bUnitId, String name) {
        String tenantId = Optional.ofNullable(TenantContext.getTenantId()).orElse("default");
        roleRepository.findByNameAndB2bUnitId(name, b2bUnitId)
                .ifPresent(r -> { throw new IllegalArgumentException("Role already exists"); });
        Role r = new Role();
        r.setName(name);
        r.setB2bUnitId(b2bUnitId);
        r.setTenantId(tenantId);
        return roleRepository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> list(UUID b2bUnitId) {
        return roleRepository.findByB2bUnitId(b2bUnitId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> listAll() {
        return roleRepository.findAll();
    }
}
