package com.skillrat.project.service.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;

import com.skillrat.project.domain.MediaModel;
import com.skillrat.project.service.AwsBlobService;
import com.skillrat.project.service.MediaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AwsBlobServiceImpl implements AwsBlobService {

    private static final String PROFILE_PICTURE = "PROFILE_PICTURE";

    @Value("${azure.storage.account-name}")
    private String accountName;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    // Support both 'account-key' and 'accountkey' property styles
    @Value("${azure.storage.account-key:}")
    private String accountKey;

    @Value("${azure.storage.accountkey:}")
    private String accountKeyAlt;

    @Autowired
    @Lazy
    private MediaService mediaService;

    // UserService is not available in this module
    // @Autowired(required = false)
    // private UserService userService;

    private BlobContainerClient getContainerClient() {
        BlobServiceClient blobServiceClient;
        if (isValidConnectionString(connectionString)) {
            blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        } else {
            String key = resolveAccountKey();
            if (isBlank(accountName) || isBlank(key)) {
                throw new IllegalArgumentException("Azure storage configuration missing: provide a valid connection string or account-name and account key");
            }
            String endpoint = "https://" + accountName + ".blob.core.windows.net";
            blobServiceClient = new BlobServiceClientBuilder()
                    .endpoint(endpoint)
                    .credential(new StorageSharedKeyCredential(accountName, key))
                    .buildClient();
        }

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
        }
        return containerClient;
    }

    private boolean isValidConnectionString(String cs) {
        if (isBlank(cs)) return false;
        String s = cs.trim();
        // Minimal sanity check for Azure connection string format
        return s.contains("AccountName=") && (s.contains("AccountKey=") || s.contains("SharedAccessSignature="));
    }

    private String resolveAccountKey() {
        if (!isBlank(accountKey)) return accountKey.trim();
        if (!isBlank(accountKeyAlt)) return accountKeyAlt.trim();
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @SuppressWarnings("resource")
    @Override
    public MediaModel uploadFile(MultipartFile file) throws IOException {
        MediaModel mediaModel = new MediaModel();
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        BlobClient blobClient = getContainerClient().getBlobClient(fileName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        mediaModel.setFileName(fileName);
        mediaModel.setUrl(getBlobUrl(fileName));
        return mediaService.saveMedia(mediaModel);
    }

    @Override
    public List<MediaModel> uploadFiles(List<MultipartFile> files) throws IOException {
        List<MediaModel> uploadFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            MediaModel media = uploadFile(file);
            uploadFiles.add(media);
        }
        return uploadFiles;
    }

    private String getBlobUrl(String fileName) {
        return "https://" + accountName + ".blob.core.windows.net/" + containerName + "/" + fileName;
    }

    @Override
    public MediaModel uploadCustomerPictureFile(Long customerId, MultipartFile file, Long createdUser)
            throws IOException {
        try {
            MediaModel picture = mediaService.findByJtcustomerAndMediaType(customerId, PROFILE_PICTURE);
            if (picture == null) {
                picture = new MediaModel();
            }
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            BlobClient blobClient = getContainerClient().getBlobClient(fileName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            picture.setUrl(getBlobUrl(fileName));
            picture.setCustomerId(customerId);
            picture.setMediaType(PROFILE_PICTURE);
            picture.setCreatedBy(createdUser != null ? createdUser : customerId);
            return mediaService.saveMedia(picture);
        } catch (Exception e) {
            throw new IOException("Failed to upload customer picture: " + e.getMessage(), e);
        }
    }



    @Override
    public MediaModel uploadIncidentMedia(Long incidentId, MultipartFile file, Long createdBy) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        BlobClient blobClient = getContainerClient().getBlobClient(fileName);
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        MediaModel media = new MediaModel();
        media.setFileName(fileName);
        media.setUrl(getBlobUrl(fileName));
        media.setMediaType(file.getContentType());
        media.setExtension(getFileExtension(file.getOriginalFilename()));
        media.setCreatedBy(createdBy);
        media.setName("Incident Media: " + file.getOriginalFilename());
        media.setIncidentId(incidentId);
        media.setActive(true);
        media.setCreationTime(new Date());

        return mediaService.saveMedia(media);
    }
    
    @Override
    public List<MediaModel> getIncidentMedia(Long incidentId) {
//        List<MediaModel> allMedia = mediaService.getAllMedia();
//        return allMedia.stream()
//                .filter(media -> incidentId.equals(media.getIncidentId()) && media.isActive())
//                .collect(Collectors.toList());
        return null;
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.'));
        }
        return "";
    }

    @Override
    public void deleteObject(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        BlobClient blobClient = getContainerClient().getBlobClient(fileName);
        blobClient.deleteIfExists();
    }




}
