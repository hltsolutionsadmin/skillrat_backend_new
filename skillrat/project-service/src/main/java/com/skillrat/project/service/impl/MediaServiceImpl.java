package com.skillrat.project.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.skillrat.project.domain.MediaModel;
import com.skillrat.project.repo.MediaRepository;
import com.skillrat.project.service.MediaService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of the MediaService interface.
 * Provides methods for managing media files and their metadata.
 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    @SuppressWarnings("null")
	@Override
    @Transactional
    public MediaModel saveMedia(@NonNull MediaModel mediaModel) {
        return mediaRepository.save(mediaModel);
    }

    @SuppressWarnings("null")
	@Override
    public MediaModel findById(@NonNull Long id) {
        return mediaRepository.findById(id).orElse(null);
    }

    @Override
    public MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType) {
        return mediaRepository.findByCustomerIdAndMediaType(
            userId != null ? userId.toString() : null, 
            mediaType
        );
    }

    @Override
    @Transactional
    public void uploadMedia(Long b2bUnitId, Object dto) {
        // Implementation not available in this module
        throw new UnsupportedOperationException("This operation is not supported in the current module");
    }

    @Override
    public List<MediaModel> findByIncidentId(UUID incidentId) {
        return mediaRepository.findByIncidentId(incidentId);
    }

    @SuppressWarnings("null")
	@Override
    @Transactional
    public void deleteMedia(@NonNull Long id) {
        mediaRepository.deleteById(id);
    }

    @Override
    public List<MediaModel> findAll() {
        return mediaRepository.findAll();
    }
}
