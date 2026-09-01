package com.schoolerp.usermanagement.modules.user.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.responsedto.RoleResponseDto;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateUserRequestDto;
import com.schoolerp.usermanagement.modules.user.responseDto.CreateUserResponseDto;
import com.schoolerp.usermanagement.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreateUserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto requestDto) {

        log.info("Create user API request received | username={} | email={} | roleId={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId());

        try {

            CreateUserResponseDto response = userService.createUser(requestDto);

            log.info("Create user API request successful | username={} | email={}", requestDto.getUserName(), requestDto.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(true, "User created successfully", response));

        } catch (Exception ex) {

            log.error("Create user API request failed | username={} | email={} | roleId={} | error={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId(), ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(false, "Failed to create user error {}" + ex.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CreateUserResponseDto>> updateUser(@Valid @PathVariable UUID id, @RequestBody CreateUserRequestDto updateRequest) {

        log.info("Role Request DTO : {}", updateRequest);
        try {
            CreateUserResponseDto updatedUser = userService.updateUserById(id, updateRequest);
            if (updatedUser != null) {
                log.info("User updated successfully");
                return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", updatedUser));
            } else {
                log.info("User updation failed");
                throw new Exception("User updation failed");
            }
        } catch (Exception e) {
            log.error("Error occurred while updating user", e.getMessage());
            throw new RuntimeException("Error occurred while updating user");
        }

    }

    @PutMapping("/password/{id}")
    public ResponseEntity<ApiResponse<CreateUserResponseDto>> updatePassword(@Valid @PathVariable UUID id, @RequestBody CreateUserRequestDto updateRequest) {

        log.info("Role Request DTO : {}", updateRequest);
        try {
            CreateUserResponseDto updatedPassword = userService.updatePasswordById(id, updateRequest);
            if (updatedPassword != null) {
                log.info("User updated successfully");
                return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", updatedPassword));
            } else {
                log.info("User Password updation failed");
                throw new Exception("User updation failed");
            }
        } catch (Exception e) {
            log.error("Error occurred while updating user password", e.getMessage());
            throw new RuntimeException("Error occurred while updating user password");
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserById(@Valid @PathVariable UUID id) {
        try {
            userService.deleteUserById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error occurred while deleting User", e.getMessage());
            throw new RuntimeException("Error occurred while deleting User");
        }

    }

    @GetMapping()
    public ResponseEntity<ApiResponse<List<CreateUserResponseDto>>> getUsers(@RequestParam(required = false) UUID roleId,@RequestParam(required = false) String search) {

        List<CreateUserResponseDto> UsersData;
        try {

            if (roleId != null) {
                UsersData = userService.getUserByRole(roleId);
                System.out.println(UsersData);
            } else {
                UsersData = userService.getUsers();
            }
            log.info("UserData{}", UsersData);
            if (!UsersData.isEmpty()) {
                log.info("Got all the data from the User service");
                return ResponseEntity.ok(new ApiResponse<>(true, "Data found", UsersData));
            } else {
                log.info("No Users data present");
                throw new Exception("Users Data fetch failed");
            }
        } catch (Exception e) {
            log.error("Error occurred while fetching users", e.getMessage());
            throw new RuntimeException("Error occurred while fetching users");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CreateUserResponseDto>> getUserById(@PathVariable UUID id) {
        try {
            CreateUserResponseDto UserData = userService.getUserById(id);
            if (UserData != null) {
                log.info("Got the data from the user service");
                return ResponseEntity.ok(new ApiResponse<>(true, "Data found", UserData));
            } else {
                log.info("No data present");
                throw new Exception("Data fetch failed");
            }
        } catch (Exception e) {
            log.error("Error occurred while fetching User", e.getMessage());
            throw new RuntimeException("Error occurred while fetching user");
        }
    }
}
