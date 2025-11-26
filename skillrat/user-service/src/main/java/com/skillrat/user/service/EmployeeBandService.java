package com.skillrat.user.service;


import com.skillrat.user.domain.EmployeeBand;
import com.skillrat.user.domain.EmployeeOrgBand;
import com.skillrat.user.repo.EmployeeBandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeBandService {

    private final EmployeeBandRepository repository;

    public EmployeeOrgBand createBand(EmployeeOrgBand band) {
        return repository.save(band);
    }

    public List<EmployeeOrgBand> getBandsByB2bUnit(UUID b2bUnitId) {
        return repository.findByB2bUnitId(b2bUnitId);
    }

    public EmployeeOrgBand getBand(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Band not found"));
    }

    public EmployeeOrgBand updateBand(UUID id, String name) {
        EmployeeOrgBand band = getBand(id);
        band.setName(name);
        return repository.save(band);
    }

    public void deleteBand(UUID id) {
        repository.deleteById(id);
    }

    public EmployeeBand[] getAllBandNames() {
        return EmployeeBand.values();
    }
}
