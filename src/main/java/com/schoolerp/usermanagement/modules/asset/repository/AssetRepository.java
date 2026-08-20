package com.schoolerp.usermanagement.modules.asset.repository;

import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.entity.ParentAssetEntity;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<AssetEntity, UUID> {

    @Query("""
            SELECT a
            FROM AssetEntity a
            WHERE a.parentAsset.id = :parentAssetId
            AND a.status = 'ACTIVE'
            """)
    List<AssetEntity> findByParentAssetId(@Param("parentAssetId") UUID parentAssetId);

}
