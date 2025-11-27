package com.skillrat.project.service;

import com.skillrat.project.domain.MediaModel;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public interface AwsBlobService {
    MediaModel uploadFile(MultipartFile file) throws FileNotFoundException, IOException;

    List<MediaModel> uploadFiles(List<MultipartFile> files) throws FileNotFoundException, IOException;

    MediaModel uploadCustomerPictureFile(Long customerId, MultipartFile file, Long createdUser)
            throws FileNotFoundException, IOException;

    /**
     * Permanently delete an object from S3 by its stored file name (key).
     */
    void deleteObject(String fileName);
    
    /**
     * Upload media file for an incident
     * @param incidentId The ID of the incident
     * @param file The file to upload
     * @param createdBy The ID of the user who created the media
     * @return The saved MediaModel
     * @throws IOException if there's an error during file upload
     */
    MediaModel uploadIncidentMedia(UUID incidentId, MultipartFile file, Long createdBy) throws IOException;
    
    /**
     * Get all media for a specific incident
     * @param incidentId The ID of the incident
     * @return List of MediaModel objects
     */
    List<MediaModel> getIncidentMedia(UUID incidentId);
}
