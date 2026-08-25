package com.schoolerp.usermanagement.modules.asset.AssetImport.service;

import com.schoolerp.usermanagement.modules.asset.AssetImport.dto.OsmImportRequestDto;

public interface OsmImportService {


    public void importAssets(OsmImportRequestDto request);
}
