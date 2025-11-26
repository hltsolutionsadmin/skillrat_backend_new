package com.skillrat.project.service;

import com.skillrat.project.domain.MediaModel;

import java.util.List;
import java.util.UUID;

public interface MediaService {

    MediaModel saveMedia(MediaModel mediaModel);
    
    MediaModel findById(Long id);
    
    MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType);
    
    void uploadMedia(Long b2bUnitId, Object dto);
    
    List<MediaModel> findByIncidentId(UUID incidentId);
    
    void deleteMedia(Long id);
    
    List<MediaModel> findAll();
}
