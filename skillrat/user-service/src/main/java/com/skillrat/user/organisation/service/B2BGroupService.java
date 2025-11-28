package com.skillrat.user.organisation.service;

import org.springframework.stereotype.Service;

import com.skillrat.user.organisation.domain.B2BGroup;
import com.skillrat.user.organisation.repo.B2BGroupRepository;

@Service
public class B2BGroupService {
	
	private B2BGroupRepository groupRepository;
	
	public B2BGroupService(B2BGroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}
	
	public B2BGroup findOrCreate(String code,String tenantId) {
		return groupRepository
        .findByCodeAndTenantId(code, tenantId)
        .orElseGet(() -> {
            B2BGroup g = new B2BGroup();
            g.setName(code);
            g.setTenantId(tenantId);
            return groupRepository.save(g);
        });
	}
}
