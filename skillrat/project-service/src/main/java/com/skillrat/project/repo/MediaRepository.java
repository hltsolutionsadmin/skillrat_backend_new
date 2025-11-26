package com.skillrat.project.repo;

import com.skillrat.project.domain.MediaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<MediaModel, Long> {

    MediaModel findByCustomerIdAndMediaType(String customerId, String mediaType);
    
    List<MediaModel> findByCustomerId(String customerId);
    

    List<MediaModel> findByIncidentId(UUID incidentId);
    

}
