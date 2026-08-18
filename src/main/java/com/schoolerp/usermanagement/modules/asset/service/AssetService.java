package com.schoolerp.usermanagement.modules.asset.service;

import com.schoolerp.usermanagement.modules.asset.requestDto.AssetrequestDto;
import com.schoolerp.usermanagement.modules.asset.responseDto.AssetresponseDto;

public interface AssetService {

    public AssetresponseDto createAsset(AssetrequestDto request);
}
