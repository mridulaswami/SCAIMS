package com.schoolerp.usermanagement.modules.asset.repository;

import com.schoolerp.usermanagement.modules.asset.entity.ParentAssetEntity;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ParentAssetRepository extends JpaRepository<ParentAssetEntity, UUID> {

    @Query(value = """
            SELECT *
            FROM parent_assets
            WHERE ST_DWithin(
                geometry::geography,
                ST_SetSRID(
                    ST_MakePoint(:longitude, :latitude),
                    4326
                )::geography,
                :radius
            )
            """, nativeQuery = true)
    List<ParentAssetEntity> findAssetsWithinRadius(@Param("longitude") double longitude, @Param("latitude") double latitude, @Param("radius") double radius);

}
