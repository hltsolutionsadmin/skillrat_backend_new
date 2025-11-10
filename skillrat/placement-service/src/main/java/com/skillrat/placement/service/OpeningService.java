package com.skillrat.placement.service;

import com.skillrat.common.tenant.TenantContext;
import com.skillrat.placement.domain.*;
import com.skillrat.placement.repo.ApplicationRepository;
import com.skillrat.placement.repo.OpeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OpeningService {
    private final OpeningRepository openingRepository;
    private final ApplicationRepository applicationRepository;

    public OpeningService(OpeningRepository openingRepository, ApplicationRepository applicationRepository) {
        this.openingRepository = openingRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Opening createOpening(UUID b2bUnitId, UUID createdByUserId, String title, String description, OpeningType type, String location) {
        Opening o = new Opening();
        o.setB2bUnitId(b2bUnitId);
        o.setCreatedByUserId(createdByUserId);
        o.setTitle(title);
        o.setDescription(description);
        o.setType(type);
        o.setLocation(location);
        o.setStatus(OpeningStatus.OPEN);
        return openingRepository.save(o);
    }

    @Transactional(readOnly = true)
    public List<Opening> listForBusiness(UUID b2bUnitId) {
        return openingRepository.findByB2bUnitId(b2bUnitId);
    }

    @Transactional(readOnly = true)
    public Optional<Opening> get(UUID id) { return openingRepository.findById(id); }

    @Transactional
    public Application apply(UUID openingId, String name, String email, String phone, String resumeUrl, UUID submittedByUserId) {
        Application a = new Application();
        a.setOpeningId(openingId);
        a.setApplicantName(name);
        a.setApplicantEmail(email);
        a.setApplicantPhone(phone);
        a.setResumeUrl(resumeUrl);
        a.setStatus(ApplicationStatus.APPLIED);
        a.setSubmittedByUserId(submittedByUserId);
        return applicationRepository.save(a);
    }

    @Transactional(readOnly = true)
    public List<Application> listApplications(UUID openingId) {
        return applicationRepository.findByOpeningId(openingId);
    }

    @Transactional
    public Optional<Application> setApplicationStatus(UUID applicationId, ApplicationStatus status) {
        return applicationRepository.findById(applicationId).map(a -> {
            a.setStatus(status);
            return applicationRepository.save(a);
        });
    }
}
