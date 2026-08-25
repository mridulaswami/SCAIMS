package com.schoolerp.usermanagement.modules.asset.AssetImport.repository;

import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.assetCategory.entity.AssetCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AssetImportRepository extends JpaRepository<AssetEntity, UUID> {


    @Query(value ="Select id from asset_category where name = :name", nativeQuery = true)
    UUID getCatId(@Param("name")  String category);


    @Modifying
    @Query(value = """
        UPDATE assets c
        SET parent_asset_id = sub.parent_id
        FROM (
            SELECT DISTINCT ON (c2.id) c2.id AS child_id, p.id AS parent_id
            FROM assets c2
            JOIN assets p
              ON ST_Contains(p.geometry, c2.geometry)
             AND p.id <> c2.id
             AND GeometryType(p.geometry) = 'POLYGON'
            WHERE GeometryType(c2.geometry) = 'POINT'
              AND c2.parent_asset_id IS NULL
            ORDER BY c2.id, ST_Area(p.geometry) ASC
        ) sub
        WHERE c.id = sub.child_id
        """, nativeQuery = true)
    int backfillParentAssignments();

}
