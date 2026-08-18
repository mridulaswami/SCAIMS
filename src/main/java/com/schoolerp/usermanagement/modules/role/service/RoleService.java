package com.schoolerp.usermanagement.modules.role.service;

import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import io.swagger.v3.oas.annotations.servers.Server;

@Server
public interface RoleService {

    public RoleRequestDto createRole(RoleRequestDto roleRequestDto);


}
