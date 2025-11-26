package com.skillrat.project.service.impl;

import com.skillrat.project.domain.MediaModel;
import com.skillrat.project.repo.MediaRepository;
import com.skillrat.project.service.MediaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the MediaService interface.
 * Provides methods for managing media files and their metadata.
 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    @Override
    @Transactional
    public MediaModel saveMedia(MediaModel mediaModel) {
        return mediaRepository.save(mediaModel);
    }

    @Override
    public MediaModel findById(Long id) {
        return mediaRepository.findById(id).orElse(null);
    }

    @Override
    public MediaModel findByJtcustomerAndMediaType(Long userId, String mediaType) {
        return mediaRepository.findByCustomerIdAndMediaType(userId, mediaType);
    }

    @Override
    @Transactional
    public void uploadMedia(Long b2bUnitId, Object dto) {
        // Implementation not available in this module
        throw new UnsupportedOperationException("This operation is not supported in the current module");
    }

    @Override
    public List<MediaModel> findByIncidentId(Long incidentId) {
        return mediaRepository.findByIncidentId(incidentId);
    }

    @Override
    @Transactional
    public void deleteMedia(Long id) {
        mediaRepository.deleteById(id);
    }

    @Override
    public List<MediaModel> findAll() {
        return mediaRepository.findAll();
    }
}
