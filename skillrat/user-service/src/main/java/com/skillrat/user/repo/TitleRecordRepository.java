package com.skillrat.user.repo;

import com.skillrat.user.domain.TitleRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TitleRecordRepository extends JpaRepository<TitleRecord, UUID> {
    List<TitleRecord> findByUserId(UUID userId);
}
