package com.schoolerp.usermanagement.modules.asset.requestDto;

import lombok.Data;

import java.util.UUID;

@Data
public class AssignAssetRequest {
    private UUID inspectorId;
}
