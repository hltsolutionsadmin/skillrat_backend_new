package com.skillrat.asset.repository;

import com.skillrat.asset.domain.AssetInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetInventoryRepository extends JpaRepository<AssetInventory, UUID> {
}
