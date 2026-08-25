package com.schoolerp.usermanagement.modules.asset.repository;

import com.schoolerp.usermanagement.modules.asset.entity.AssetEntity;
import com.schoolerp.usermanagement.modules.asset.entity.ParentAssetEntity;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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


    @Query(value = """
            WITH nearby_parents AS (
                SELECT
                    p.id,
                    ST_Distance(
                        p.geometry::geography,
                        ST_SetSRID(
                            ST_MakePoint(:longitude, :latitude),
                            4326
                        )::geography
                    ) AS distance
                FROM assets p
                WHERE p.parent_asset_id IS NULL
                  AND ST_DWithin(
                      p.geometry::geography,
                      ST_SetSRID(
                          ST_MakePoint(:longitude, :latitude),
                          4326
                      )::geography,
                      :radius
                  )
            )
            SELECT a.*
            FROM assets a
            INNER JOIN nearby_parents p
                ON a.id = p.id
                OR a.parent_asset_id = p.id
            ORDER BY
                p.distance,
                CASE
                    WHEN a.id = p.id THEN 0
                    ELSE 1
                END,
                a.name
            """, nativeQuery = true)
    List<AssetEntity> findNearbyParentsWithChildren(@Param("longitude") double longitude, @Param("latitude") double latitude, @Param("radius") double radius);



    @Modifying
    @Query(value = """
    INSERT INTO assets (id, name, category_id, geometry, source_type, source_id, installed_date, last_inspected_date)
    VALUES (gen_random_uuid(), :name, :categoryId, ST_GeomFromText(:wkt, 4326), :sourceType, :sourceId, current_timestamp, current_timestamp)
    ON CONFLICT (source_type, source_id) DO NOTHING
    """, nativeQuery = true)
    void upsertAsset(@Param("name") String name,
                     @Param("categoryId") UUID categoryId,
                     @Param("wkt") String wkt,
                     @Param("sourceType") String sourceType,
                     @Param("sourceId") long sourceId);

}
