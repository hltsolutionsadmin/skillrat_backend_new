package com.skillrat.project.service.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class MediaDTO {

    private Long id;

    private Long customerId;

    private String url;

    private String timeSlot;

    private String fileName;

    private String mediaType;

    private String description;

    private String extension;

    private boolean active;

    private Long createdBy;

    private String creationTime;

    private String modificationTime;

    private List<MultipartFile> mediaFiles;
    private List<String> mediaUrls;


}
