package com.schoolerp.usermanagement.modules.role.service.impl;

import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import com.schoolerp.usermanagement.modules.role.repository.RoleEntityRepository;
import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.responsedto.RoleResponseDto;
import com.schoolerp.usermanagement.modules.role.service.RoleService;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleEntityRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;
    //  private final RoleService roleService;

    @Override
    @Transactional
    public RoleResponseDto createRole(RoleRequestDto request) {
        log.info("Role Request DTO : {}", request);

        try {
            RoleEntity role = RoleEntity.builder().roleName(request.getRoleName()).description(request.getRoleDescription()).build();
            roleRepository.save(role);
            log.info("Role Created successfully : {}", role);
            return RoleResponseDto.builder().id(role.getId()).roleName(role.getRoleName()).roleDescription(role.getDescription()).build();
        } catch (Exception e) {
            log.error("Error while creating role : {}", e.getMessage());
            throw new RuntimeException("Error while creating role" + e.getMessage());
        }
    }

    @Override
    public List<RoleResponseDto> getRoles() {
        try {
            List<RoleEntity> roles = roleRepository.findAll();
            return roles.stream().map(role -> RoleResponseDto.builder().id(role.getId()).roleName(role.getRoleName()).roleDescription(role.getDescription()).build()).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error while getting roles from database");
        }
    }

    @Override
    public RoleResponseDto getRoleById(Integer id) {
        try {
            Optional<RoleEntity> roleData = roleRepository.findById(id);
            RoleEntity role = roleData.orElseThrow(() -> new RuntimeException("Role not found"));

            return RoleResponseDto.builder().id(role.getId()).roleName(role.getRoleName()).roleDescription(role.getDescription()).build();
        } catch (Exception e) {
            log.error("Error while getting role : {}", e.getMessage());
            throw new RuntimeException("Error while getting role" + e.getMessage());
        }
    }


    @Override
    public RoleResponseDto updateRoleById(Integer id, RoleRequestDto updateRequest) {
        try {

            RoleEntity getData = roleRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found"));
            getData.setRoleName(updateRequest.getRoleName());
            getData.setDescription(updateRequest.getRoleDescription());

            RoleEntity updatedRole = roleRepository.save(getData);
            log.info("Role Updated successfully : {}", updatedRole);
            return RoleResponseDto.builder().id(updatedRole.getId()).roleName(updatedRole.getRoleName()).roleDescription(updatedRole.getDescription()).build();
        } catch (Exception e) {
            log.error("Error while updating role : {}", e.getMessage());
            throw new RuntimeException("Error while updating role" + e.getMessage());
        }


    }

    @Override
    public void deleteRoleById(Integer id) {
        try {
            roleRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error while deleting role : {}", e.getMessage());
            throw new RuntimeException("Error while deleting role" + e.getMessage());
        }
    }
}
