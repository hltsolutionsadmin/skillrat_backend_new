package com.skillrat.user.service;

import com.skillrat.user.domain.EmployeeBand;
import com.skillrat.user.domain.EmployeeOrgBand;
import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.repo.B2BUnitRepository;
import com.skillrat.user.repo.EmployeeBandRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeBandService {

    private final EmployeeBandRepository repository;
    private final B2BUnitRepository b2bUnitRepository;

    @SuppressWarnings("null")
	@Transactional
    public EmployeeOrgBand createBand(EmployeeOrgBand band) {
        // Fetch the existing B2BUnit from the database
        B2BUnit existingB2BUnit = b2bUnitRepository.findById(band.getB2bUnit().getId())
            .orElseThrow(() -> new IllegalArgumentException("B2BUnit not found with id: " + band.getB2bUnit().getId()));
        
        // Set the managed entity
        band.setB2bUnit(existingB2BUnit);
        
        return repository.save(band);
    }

    public List<EmployeeOrgBand> getBandsByB2bUnit(UUID b2bUnitId) {
        return repository.findByB2bUnit_Id(b2bUnitId);
    }

    public EmployeeOrgBand getBand(@NonNull UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Band not found"));
    }

    @SuppressWarnings("null")
	public EmployeeOrgBand updateBand(@NonNull UUID id, EmployeeOrgBand orgBand) {
        EmployeeOrgBand band = getBand(id);
        if(orgBand.getName()!=null) {
            band.setName(orgBand.getName());
        }
        if(orgBand.getExperienceMin()!=null) {
            band.setExperienceMin(orgBand.getExperienceMin());
        }
        if(orgBand.getExperienceMax()!=null) {
            band.setExperienceMax(orgBand.getExperienceMax());
        }
        if(orgBand.getSalary()!=null) {
            band.setSalary(orgBand.getSalary());
        }
        return repository.save(band);
    }

    public void deleteBand(@NonNull UUID id) {
        repository.deleteById(id);
    }

    public EmployeeBand[] getAllBandNames() {
        return EmployeeBand.values();
    }
}
