package com.schoolerp.usermanagement.modules.role.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoleRequestDto>> createRole(@Valid @RequestBody RoleRequestDto request) {

        log.info("Role Request DTO : {}", request);
        try {
            RoleRequestDto createdRole = roleService.createRole(request);
            if (createdRole != null) {
                log.info("Role created successfully");
                return ResponseEntity.ok(new ApiResponse<>(true, "Role created successfully", createdRole));
            } else {
                log.info("Role creation failed");
                throw new Exception("Role creation failed");
            }
        } catch (Exception e) {
            log.error("Error occurred while creating role", e.getMessage());
            throw new RuntimeException("Error occurred while creating role");
        }

    }

    @GetMapping("/me")
    public Object me(Authentication authentication) {
        return authentication.getAuthorities();

    }

}

