package com.schoolerp.usermanagement.modules.asset.repository;

import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssetRepository extends JpaRepository<AssetEntity , UUID> {
}
