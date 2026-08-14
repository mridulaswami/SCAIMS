package com.schoolerp.usermanagement.modules.user.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateUserRequestDto;
import com.schoolerp.usermanagement.modules.user.responseDto.CreateUserResponseDto;
import com.schoolerp.usermanagement.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auth/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
}
