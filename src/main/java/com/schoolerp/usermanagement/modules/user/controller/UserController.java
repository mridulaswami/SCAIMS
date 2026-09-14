package com.schoolerp.usermanagement.modules.user.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.responsedto.RoleResponseDto;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.requestDto.ChangePasswordRequestDto;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateFieldEngineerDto;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateUserRequestDto;
import com.schoolerp.usermanagement.modules.user.requestDto.SendOptRequestDto;
import com.schoolerp.usermanagement.modules.user.responseDto.CreateUserResponseDto;
import com.schoolerp.usermanagement.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody SendOptRequestDto requestDto) {

        log.info("Register API request received  | email={}", requestDto.getEmail());

        try {

            userService.register(requestDto);

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "OTP sent successfully. Please verify your email.", null));

        } catch (Exception ex) {

            log.error("Register API failed | email={} | error={}", requestDto.getEmail(), ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(false, "Failed to register user: " + ex.getMessage(), null));
        }
    }

    @PostMapping("/registerFieldEngineer")
    public ResponseEntity<ApiResponse<CreateUserResponseDto>> createFieldEngineer(@Valid @RequestBody CreateFieldEngineerDto requestDto) {

        log.info("Create Field Engineer API request received | username={} | email={} | roleId={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId());

        try {

            CreateUserResponseDto response = userService.registerFieldEngineer(requestDto);

            log.info("Create Field Engineer API request successful | username={} | email={}", requestDto.getUserName(), requestDto.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(true, "Field Engineer created successfully", response));

        } catch (Exception ex) {

            log.error("Create Field Engineer API request failed | username={} | email={} | roleId={} | error={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId(), ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(false, "Failed to create Field Engineer error: " + ex.getMessage(), null));
        }
    }

    @PostMapping("/verify")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreateUserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto requestDto) {

        log.info("Create user API request received | username={} | email={} | roleId={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId());

        try {

            CreateUserResponseDto response = userService.verifyOtp(requestDto);

            log.info("Create user API request successful | username={} | email={}", requestDto.getUserName(), requestDto.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(true, "User created successfully", response));

        } catch (Exception ex) {

            log.error("Create user API request failed | username={} | email={} | roleId={} | error={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId(), ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(false, "Failed to create user error: " + ex.getMessage(), null));
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto) {

        log.info("Change password API request received | username={}", requestDto.getUserName());

        try {

            userService.changePassword(requestDto);

            log.info("Change password API request successful | username={}", requestDto.getUserName());

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Password Change Successfull", null));

        } catch (Exception ex) {

            log.error("Change password API request failed | username={} | error={}", requestDto.getUserName(), ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(false, "Failed Change Password error: " + ex.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CreateUserResponseDto>> updateUser(@Valid @PathVariable UUID id, @RequestBody CreateFieldEngineerDto updateRequest) {

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
    public ResponseEntity<ApiResponse<Page<CreateUserResponseDto>>> getUsers(@PageableDefault(size = 5)  Pageable pageable , @RequestParam(required = false) Integer roleId,
                                                                             @RequestParam(required = false) String search) {

        try {
            Page<CreateUserResponseDto> usersData = userService.getUsers(pageable, roleId, search);
            log.info("UserData {}", usersData);
            String message = usersData.isEmpty() ? "No users found" : "Data found";
            return ResponseEntity.ok(new ApiResponse<>(!usersData.isEmpty(), message, usersData));
        } catch (Exception e) {
            log.error("Error occurred while fetching users", e);
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
