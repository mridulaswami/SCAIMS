package com.schoolerp.usermanagement.modules.assetCategory.repository;

import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AssetCategoryRepository extends JpaRepository<AssetCategoryEntity, UUID> {

    @Modifying
    @Query(value = """
        INSERT INTO asset_category (name, icon_key)
        VALUES (:name , :icon_key)
        ON CONFLICT (name) DO NOTHING
        """, nativeQuery = true)
    void upsert(@Param("name") String name,
        @Param("icon_key") String icon_key);




}
