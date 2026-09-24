package com.schoolerp.usermanagement.modules.asset.AssetImport.service;

import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.OsmImportRequestDto;

public interface OsmImportService {


    public Integer importAssets(OsmImportRequestDto request);
}
