package com.skillrat.asset.repository;

import com.skillrat.asset.domain.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, UUID> {
}
