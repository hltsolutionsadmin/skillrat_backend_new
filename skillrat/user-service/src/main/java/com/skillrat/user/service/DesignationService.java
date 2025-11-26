package com.skillrat.user.service;

import com.skillrat.user.domain.Designation;
import com.skillrat.user.dto.DesignationDTO;
import com.skillrat.user.dto.DesignationRequestDTO;
import com.skillrat.user.dto.DesignationResponseDTO;
import com.skillrat.user.repo.DesignationRepository;
import com.skillrat.user.repo.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;

    public DesignationService(DesignationRepository designationRepository,
                              EmployeeRepository employeeRepository) {
        this.designationRepository = designationRepository;
        this.employeeRepository = employeeRepository;
    }
    public List<DesignationDTO> getDesignations(UUID b2bUnitId) {
        return employeeRepository.findDesignationWithBandAndResourceCount(b2bUnitId);
    }


    // List with band counts
    public List<DesignationResponseDTO> getAllDesignations() {
        List<Designation> designations = designationRepository.findAll();
        List<DesignationResponseDTO> result = new ArrayList<>();

        for (Designation desig : designations) {
            DesignationResponseDTO dto = new DesignationResponseDTO();
            dto.setDesignationId(desig.getId());
            dto.setDesignationName(desig.getName());
            result.add(dto);
        }

        return result;
    }

    // Create
    public Designation createDesignation(DesignationRequestDTO request) {
        if (designationRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Designation already exists");
        }
        Designation desig = new Designation();
        desig.setName(request.getName());
        desig.setB2bUnitId(request.getB2bUnitId());
        return designationRepository.save(desig);
    }

    // Update
    public Designation updateDesignation(UUID id, DesignationRequestDTO request) {
        Designation desig = designationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Designation not found"));
        desig.setName(request.getName());
        return designationRepository.save(desig);
    }

    // Delete
    public void deleteDesignation(UUID id) {
        if (!designationRepository.existsById(id)) {
            throw new NoSuchElementException("Designation not found");
        }
        designationRepository.deleteById(id);
    }

    public Designation findById(UUID designation) {
       return designationRepository.findById(designation)
                .orElseThrow(() -> new NoSuchElementException("Designation not found"));
    }

    public List<DesignationDTO> getAllDesignations(UUID b2bUnitId) {
        List<DesignationDTO> list=new ArrayList<>();
        List<Designation> desiList= designationRepository.findAllByB2bUnitId(b2bUnitId);
        for(Designation desig:desiList){
            DesignationDTO dto=new DesignationDTO();
            dto.setDesignationId(desig.getId());
            dto.setDesignationName(desig.getName());
            list.add(dto);
        }
        return list;
    }
}

