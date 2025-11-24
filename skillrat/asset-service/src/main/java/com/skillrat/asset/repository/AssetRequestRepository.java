package com.skillrat.asset.repository;

import com.skillrat.asset.domain.AssetRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetRequestRepository extends JpaRepository<AssetRequest, UUID> {
}
