package com.schoolerp.usermanagement.modules.role.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.responsedto.RoleResponseDto;
import com.schoolerp.usermanagement.modules.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(@Valid @RequestBody RoleRequestDto request) {

        log.info("Role Request DTO : {}", request);
        try {
            RoleResponseDto createdRole = roleService.createRole(request);
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> updateRole(@Valid @PathVariable Integer id, @RequestBody RoleRequestDto updateRequest) {

        log.info("Role Request DTO : {}", updateRequest);
        try {
            RoleResponseDto updatedRole = roleService.updateRoleById(id, updateRequest);
            if (updatedRole != null) {
                log.info("Role updated successfully");
                return ResponseEntity.ok(new ApiResponse<>(true, "Role updated successfully", updatedRole));
            } else {
                log.info("Role updation failed");
                throw new Exception("Role creation failed");
            }
        } catch (Exception e) {
            log.error("Error occurred while updating role", e.getMessage());
            throw new RuntimeException("Error occurred while updating role");
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoleById(@Valid @PathVariable Integer id) {
        try {
            roleService.deleteRoleById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error occurred while deleting role", e.getMessage());
            throw new RuntimeException("Error occurred while deleting role");
        }

    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<RoleResponseDto>>> getRole() {
        try {
            List<RoleResponseDto> rolesData = roleService.getRoles();
            if (rolesData != null) {
                log.info("Got all the data from the role service");
                return ResponseEntity.ok(new ApiResponse<>(true, "Data found", rolesData));
            } else {
                log.info("No roles data present");
                throw new Exception("Roles Data fetch failed");
            }
        } catch (Exception e) {
            log.error("Error occurred while fetching roles", e.getMessage());
            throw new RuntimeException("Error occurred while fetching roles");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponseDto>> getRoleById(@PathVariable Integer id) {
        try {
            RoleResponseDto roleData = roleService.getRoleById(id);
            if (roleData != null) {
                log.info("Got the data from the role service");
                return ResponseEntity.ok(new ApiResponse<>(true, "Data found", roleData));
            } else {
                log.info("No data present");
                throw new Exception("Data fetch failed");
            }
        } catch (Exception e) {
            log.error("Error occurred while fetching role", e.getMessage());
            throw new RuntimeException("Error occurred while fetching role");
        }
    }

    @GetMapping("/me")
    public Object me(Authentication authentication) {
        return authentication.getAuthorities();

    }

}

