package com.schoolerp.usermanagement.modules.assetCategory.repository;

import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetCategoryRepository extends JpaRepository<AssetCategoryEntity, UUID> {
}
