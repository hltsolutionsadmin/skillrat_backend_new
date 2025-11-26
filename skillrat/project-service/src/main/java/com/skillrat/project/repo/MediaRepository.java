package com.skillrat.project.repo;

import com.skillrat.project.domain.MediaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<MediaModel, Long> {

    MediaModel findByCustomerIdAndMediaType(Long userId, String mediaType);
    
    List<MediaModel> findByCustomerId(Long userId);
    

    List<MediaModel> findByIncidentId(Long incidentId);
    
    // Standard JpaRepository methods already available:
    // - save()
    // - findById()
    // - findAll()
    // - deleteById()
}
