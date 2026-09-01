package com.schoolerp.usermanagement.modules.role.service;

import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.responsedto.RoleResponseDto;
import io.swagger.v3.oas.annotations.servers.Server;

import java.util.List;
import java.util.UUID;

@Server
public interface RoleService {

    public RoleResponseDto createRole(RoleRequestDto roleRequestDto);

    public List<RoleResponseDto> getRoles();
    public RoleResponseDto getRoleById(Integer id);
    public RoleResponseDto updateRoleById( Integer id, RoleRequestDto roleRequestDto);

    public void deleteRoleById( Integer id);


}
