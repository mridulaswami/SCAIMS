package com.schoolerp.usermanagement.modules.auth.controller;

import com.schoolerp.usermanagement.common.response.ApiResponse;
import com.schoolerp.usermanagement.modules.auth.requestDto.ForgetPasswordRequestDto;
import com.schoolerp.usermanagement.modules.auth.requestDto.LoginRequestDto;
import com.schoolerp.usermanagement.modules.auth.responseDto.LoginResponseDto;
import com.schoolerp.usermanagement.modules.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto, HttpServletResponse response) {

        log.info("POST /api/v1/auth/login | username={}", requestDto.getUserName());

        try {

            LoginResponseDto loginResponse = authService.login(requestDto, response);

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Login successful", loginResponse));

        } catch (Exception ex) {

            log.error("POST /api/v1/auth/login failed | username={} | error={}", requestDto.getUserName(), ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.of(false, "Failed to Login User : " + ex.getMessage(), null));
        }
    }

    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDto>> refreshToken(HttpServletRequest request, HttpServletResponse res) {

        log.info("POST /api/v1/auth/refresh request received");

        try {

            String refreshToken = extractRefreshToken(request);

            LoginResponseDto response = authService.refreshToken(refreshToken, res);

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Access token refreshed successfully", response));

        } catch (Exception ex) {

            log.error("POST /api/v1/auth/refresh failed | error={}", ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.of(false, "Invalid or expired refresh token", null));
        }
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {

        log.info("POST /api/v1/auth/logout request received");

        try {

            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader == null || authorizationHeader.isBlank()) {

                log.warn("Logout failed - Authorization header missing");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.of(false, "Authorization token is required", null));
            }

            if (!authorizationHeader.startsWith("Bearer ")) {

                log.warn("Logout failed - invalid Authorization header");

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.of(false, "Invalid authorization token", null));
            }

            String accessToken = authorizationHeader.substring(7);

            authService.logout(accessToken, response);

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Logout successful", null));

        } catch (Exception ex) {

            log.error("POST /api/v1/auth/logout failed | error={}", ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(false, "Logout failed", null));
        }
    }

    // =========================================================
    // EXTRACT REFRESH TOKEN FROM COOKIE
    // =========================================================

    private String extractRefreshToken(HttpServletRequest request) {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new RuntimeException("Authorization header not found");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header");
        }

        String refreshToken = authorizationHeader.substring(7).trim();

        if (refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token not found");
        }

        return refreshToken;
    }

    @PostMapping("/forgetPassword")
    public ResponseEntity<ApiResponse<Void>> forgetPassword(@RequestBody ForgetPasswordRequestDto requestDto) {

        try {

            log.info("POST /api/v1/auth/forgetPassword | Request received | identifier={}", requestDto.getUserName());

            authService.forgetPassword(requestDto);

            log.info("POST /api/v1/auth/forgetPassword | Password reset successful | identifier={}", requestDto.getUserName());

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of(true, "Forget Password Successful. New password has been sent to your registered email.", null));

        } catch (IllegalArgumentException ex) {

            log.warn("POST /api/v1/auth/forgetPassword | Validation failed | error={}", ex.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.of(false, ex.getMessage(), null));

        } catch (RuntimeException ex) {

            log.error("POST /api/v1/auth/forgetPassword | Failed | error={}", ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.of(false, "Forget Password Failed. Please try again later error=" + ex.getMessage(), null));
        }
    }
}