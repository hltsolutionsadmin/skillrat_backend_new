package com.skillrat.project.service;

import com.skillrat.project.domain.Incident;
import com.skillrat.project.domain.MediaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncidentMediaService {
    private static final Logger log = LoggerFactory.getLogger(IncidentMediaService.class);
    
    private final AwsBlobService awsBlobService;
    private final MediaService mediaService;

    public IncidentMediaService(AwsBlobService awsBlobService, MediaService mediaService) {
        this.awsBlobService = awsBlobService;
        this.mediaService = mediaService;
    }

    @Transactional
    public List<MediaModel> handleIncidentMedia(Incident incident, List<MultipartFile> mediaFiles, List<String> mediaUrls) {
        List<MediaModel> savedMedia = new ArrayList<>();
        
        // Handle file uploads
        if (mediaFiles != null) {
            savedMedia.addAll(processMediaFiles(incident, mediaFiles));
        }
        
        // Handle media URLs
        if (mediaUrls != null) {
            savedMedia.addAll(processMediaUrls(incident, mediaUrls));
        }
        
        return savedMedia;
    }
    
    private List<MediaModel> processMediaFiles(Incident incident, List<MultipartFile> mediaFiles) {
        List<MediaModel> savedMedia = new ArrayList<>();
        
        for (MultipartFile file : mediaFiles) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            
            try {
                MediaModel media = awsBlobService.uploadFile(file);
                media.setMediaType("INCIDENT_ATTACHMENT");
                incident.addMedia(media);
                media = mediaService.saveMedia(media);
                savedMedia.add(media);
                log.info("Successfully uploaded media file: {}", file.getOriginalFilename());
            } catch (IOException e) {
                log.error("Failed to upload media file: {}", file.getOriginalFilename(), e);
                // Continue with other files if one fails
            }
        }
        
        return savedMedia;
    }
    
    private List<MediaModel> processMediaUrls(Incident incident, List<String> mediaUrls) {
        List<MediaModel> savedMedia = new ArrayList<>();
        
        for (String url : mediaUrls) {
            if (url == null || url.trim().isEmpty()) {
                continue;
            }
            
            try {
                MediaModel media = new MediaModel();
                media.setUrl(url);
                media.setMediaType("INCIDENT_ATTACHMENT_URL");
                incident.addMedia(media);
                media = mediaService.saveMedia(media);
                savedMedia.add(media);
                log.info("Successfully saved media URL: {}", url);
            } catch (Exception e) {
                log.error("Failed to save media URL: {}", url, e);
                // Continue with other URLs if one fails
            }
        }
        
        return savedMedia;
    }
}
