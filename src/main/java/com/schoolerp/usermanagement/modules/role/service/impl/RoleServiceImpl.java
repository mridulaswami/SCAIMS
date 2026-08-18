package com.schoolerp.usermanagement.modules.role.service.impl;

import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import com.schoolerp.usermanagement.modules.role.repository.RoleEntityRepository;
import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.service.RoleService;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleEntityRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public RoleRequestDto createRole(RoleRequestDto request) {
        log.info("Role Request DTO : {}", request);

        try {
            RoleEntity role = RoleEntity.builder().
                    roleName(request.getRoleName()).
                    description(request.getRoleDescription()).
                    build();
            roleRepository.save(role);
            log.info("Role Created successfully : {}", role);
            return request;
        } catch (Exception e) {
            log.error("Error while creating role : {}", e.getMessage());
            throw new RuntimeException("Error while creating role" + e.getMessage());
        }
    }
}
