package com.schoolerp.usermanagement.modules.auth.service.impl;

import com.schoolerp.usermanagement.common.util.PasswordGenerator;
import com.schoolerp.usermanagement.modules.auth.entity.AuthEntity;
import com.schoolerp.usermanagement.modules.auth.entity.UserRoleEntity;
import com.schoolerp.usermanagement.modules.auth.repository.AuthEntityRepository;
import com.schoolerp.usermanagement.modules.auth.repository.UserRoleRepository;
import com.schoolerp.usermanagement.modules.auth.requestDto.ForgetPasswordRequestDto;
import com.schoolerp.usermanagement.modules.auth.requestDto.LoginRequestDto;
import com.schoolerp.usermanagement.modules.auth.responseDto.LoginResponseDto;
import com.schoolerp.usermanagement.modules.auth.service.AuthService;
import com.schoolerp.usermanagement.modules.email.constant.EmailSubjectConstant;
import com.schoolerp.usermanagement.modules.email.constant.EmailTemplateConstant;
import com.schoolerp.usermanagement.modules.email.requestDto.EmailRequestDto;
import com.schoolerp.usermanagement.modules.email.service.EmailService;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import com.schoolerp.usermanagement.security.JwtTokenProvider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserEntityRepository userRepository;
    private final AuthEntityRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /*
     * Access Token Expiration
     * Default = 15 minutes
     */
    @Value("${app.jwt.expiration-ms:900000}")
    private long accessTokenExpirationMs;

    /*
     * Refresh Token Expiration
     * Default = 7 days
     */
    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";


    // =========================================================
    // LOGIN
    // =========================================================

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response) {

        log.info("Login request received | username={}", requestDto.getUserName());

        try {

            // =====================================================
            // 1. AUTHENTICATE USERNAME + PASSWORD
            // =====================================================

            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestDto.getUserName(), requestDto.getPassword()));

            log.debug("User authentication successful | username={}", requestDto.getUserName());


            // =====================================================
            // 2. FIND USER
            // =====================================================

            UserEntity user = userRepository.findByUserName(requestDto.getUserName()).orElseThrow(() -> {

                log.warn("Login failed - user not found | username={}", requestDto.getUserName());

                return new RuntimeException("User not found");
            });


            // =====================================================
            // 3. CHECK USER STATUS
            // =====================================================


            if (!user.isStatus()) {

                log.warn("Login rejected - user inactive | userId={} | username={}", user.getId(), user.getUserName());

                throw new RuntimeException("User account is inactive");
            }


            // =====================================================
            // 4. GET ROLE
            // =====================================================
            UserRoleEntity userRole = userRoleRepository.findByUserId(user.getId()).stream().findFirst().orElseThrow(() -> new RuntimeException("Role not assigned to user"));
            String role = userRole.getRole().getRoleName();

            log.debug("User role loaded | userId={} | username={} | role={}", user.getId(), user.getUserName(), role);


            // =====================================================
            // 5. GENERATE ACCESS TOKEN
            // =====================================================

            String accessToken = jwtTokenProvider.generateAccessToken(user.getUserName(), user.getId(), role);


            // =====================================================
            // 6. GENERATE REFRESH TOKEN
            // =====================================================

            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserName());


            // =====================================================
            // 7. CALCULATE TOKEN EXPIRATION
            // =====================================================

            LocalDateTime now = LocalDateTime.now();

            LocalDateTime accessTokenExpireAt = now.plus(Duration.ofMillis(accessTokenExpirationMs));

            LocalDateTime refreshTokenExpireAt = now.plus(Duration.ofMillis(refreshTokenExpirationMs));

            log.debug("Token expiry calculated | accessExpireAt={} | refreshExpireAt={}", accessTokenExpireAt, refreshTokenExpireAt);


            // =====================================================
            // 8. FIND EXISTING AUTH ENTITY
            // =====================================================

            AuthEntity authEntity = authRepository.findByUserId(user).orElseGet(AuthEntity::new);


            // =====================================================
            // 9. SET AUTH DATA
            // =====================================================

            authEntity.setUserId(user);

            authEntity.setAccessToken(accessToken);

            authEntity.setRefreshToken(refreshToken);

            authEntity.setAccessTokenExpireAt(accessTokenExpireAt);

            authEntity.setRefreshTokenExpireAt(refreshTokenExpireAt);


            // =====================================================
            // 10. SAVE AUTH ENTITY
            // =====================================================

            authRepository.save(authEntity);

            log.info("Auth token saved successfully | userId={} | username={}", user.getId(), user.getUserName());


            // =====================================================
            // 11. ADD REFRESH TOKEN COOKIE
            // =====================================================

            addRefreshTokenCookie(response, refreshToken);


            // =====================================================
            // 12. LOGIN SUCCESS LOG
            // =====================================================

            log.info("Login successful | userId={} | username={} | role={}", user.getId(), user.getUserName(), role);


            // =====================================================
            // 13. BUILD RESPONSE
            // =====================================================

            return LoginResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer").user(user).build();


        } catch (BadCredentialsException ex) {

            log.warn("Login failed - invalid credentials | username={}", requestDto.getUserName());

            throw ex;

        } catch (RuntimeException ex) {

            log.error("Login failed | username={} | error={}", requestDto.getUserName(), ex.getMessage(), ex);

            throw ex;
        }
    }


    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Override
    @Transactional
    public LoginResponseDto refreshToken(String refreshToken, HttpServletResponse response) {

        log.info("Refresh token request received");

        try {

            // =====================================================
            // 1. CHECK REFRESH TOKEN
            // =====================================================

            if (refreshToken == null || refreshToken.isBlank()) {

                log.warn("Refresh token rejected - token missing");

                throw new RuntimeException("Refresh token is required");
            }


            // =====================================================
            // 2. VALIDATE JWT
            // =====================================================

            if (!jwtTokenProvider.validateToken(refreshToken)) {

                log.warn("Refresh token validation failed");

                throw new RuntimeException("Invalid refresh token");
            }


            // =====================================================
            // 3. CHECK TOKEN TYPE
            // =====================================================

            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {

                log.warn("Invalid token type - expected REFRESH");

                throw new RuntimeException("Invalid refresh token type");
            }


            // =====================================================
            // 4. EXTRACT USERNAME
            // =====================================================

            String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);


            // =====================================================
            // 5. FIND USER
            // =====================================================

            UserEntity user = userRepository.findByUserName(username).orElseThrow(() -> {

                log.warn("Refresh failed - user not found | username={}", username);

                return new RuntimeException("User not found");
            });


            // =====================================================
            // 6. CHECK USER STATUS
            // =====================================================

            /*
            if (!user.isStatus()) {

                log.warn(
                        "Refresh rejected - user inactive | userId={} | username={}",
                        user.getId(),
                        user.getUserName()
                );

                throw new RuntimeException(
                        "User account is inactive"
                );
            }
            */


            // =====================================================
            // 7. GET ROLE
            // =====================================================

            UserRoleEntity userRole = userRoleRepository.findByUserId(user.getId()).stream().findFirst().orElseThrow(() -> new RuntimeException("Role not assigned to user"));
            String role = userRole.getRole().getRoleName();


            // =====================================================
            // 8. GENERATE NEW ACCESS TOKEN
            // =====================================================

            String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUserName(), user.getId(), role);


            // =====================================================
            // 9. GENERATE NEW REFRESH TOKEN
            // =====================================================

            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserName());


            // =====================================================
            // 10. CALCULATE NEW TOKEN EXPIRATION
            // =====================================================

            LocalDateTime now = LocalDateTime.now();

            LocalDateTime newAccessTokenExpireAt = now.plus(Duration.ofMillis(accessTokenExpirationMs));

            LocalDateTime newRefreshTokenExpireAt = now.plus(Duration.ofMillis(refreshTokenExpirationMs));


            // =====================================================
            // 11. FIND EXISTING AUTH ENTITY
            // =====================================================

            AuthEntity authEntity = authRepository.findByUserId(user).orElseThrow(() -> {

                log.warn("Auth entry not found | userId={}", user.getId());

                return new RuntimeException("Authentication session not found");
            });


            // =====================================================
            // 12. UPDATE AUTH ENTITY
            // =====================================================

            authEntity.setAccessToken(newAccessToken);

            authEntity.setRefreshToken(newRefreshToken);

            authEntity.setAccessTokenExpireAt(newAccessTokenExpireAt);

            authEntity.setRefreshTokenExpireAt(newRefreshTokenExpireAt);


            // =====================================================
            // 13. SAVE UPDATED AUTH ENTITY
            // =====================================================

            authRepository.save(authEntity);


            log.info("Auth tokens updated successfully | userId={} | username={}", user.getId(), user.getUserName());


            // =====================================================
            // 14. UPDATE REFRESH TOKEN COOKIE
            // =====================================================

            addRefreshTokenCookie(response, newRefreshToken);


            // =====================================================
            // 15. SUCCESS LOG
            // =====================================================

            log.info("Access and refresh tokens refreshed successfully | userId={} | username={} | role={}", user.getId(), user.getUserName(), role);


            // =====================================================
            // 16. RETURN NEW TOKENS
            // =====================================================

            return LoginResponseDto.builder().accessToken(newAccessToken).refreshToken(newRefreshToken).tokenType("Bearer").user(user).build();


        } catch (RuntimeException ex) {

            log.error("Refresh token failed | error={}", ex.getMessage(), ex);

            throw ex;
        }
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @Override
    @Transactional
    public void logout(String accessToken, HttpServletResponse response) {

        log.info("Logout request received");

        try {

            // =====================================================
            // 1. CHECK ACCESS TOKEN
            // =====================================================

            if (accessToken == null || accessToken.isBlank()) {

                log.warn("Logout failed - access token missing");

                throw new RuntimeException("Access token is required");
            }


            // =====================================================
            // 2. REMOVE BEARER PREFIX
            // =====================================================

            if (accessToken.startsWith("Bearer ")) {

                accessToken = accessToken.substring(7);
            }


            // =====================================================
            // 3. VALIDATE ACCESS TOKEN
            // =====================================================

            if (!jwtTokenProvider.validateToken(accessToken)) {

                log.warn("Logout failed - invalid access token");

                throw new RuntimeException("Invalid access token");
            }


            // =====================================================
            // 4. MAKE SURE TOKEN IS ACCESS TOKEN
            // =====================================================

            if (!jwtTokenProvider.validateAccessToken(accessToken)) {

                log.warn("Logout failed - token is not ACCESS token");

                throw new RuntimeException("Invalid access token type");
            }


            // =====================================================
            // 5. EXTRACT USERNAME
            // =====================================================

            String username = jwtTokenProvider.getUsernameFromJWT(accessToken);


            // =====================================================
            // 6. FIND USER
            // =====================================================

            UserEntity user = userRepository.findByUserName(username).orElseThrow(() -> {

                log.warn("Logout failed - user not found | username={}", username);

                return new RuntimeException("User not found");
            });


            // =====================================================
            // 7. DELETE AUTH ENTRY
            // =====================================================

            authRepository.deleteByUserId(user);


            // =====================================================
            // 8. CLEAR REFRESH TOKEN COOKIE
            // =====================================================

            clearRefreshTokenCookie(response);


            // =====================================================
            // 9. SUCCESS LOG
            // =====================================================

            log.info("Logout successful | userId={} | username={} | auth entry deleted", user.getId(), user.getUserName());


        } catch (RuntimeException ex) {

            log.error("Logout failed | error={}", ex.getMessage(), ex);

            throw ex;
        }
    }


    // =========================================================
    // ADD REFRESH TOKEN COOKIE
    // =========================================================

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);

        cookie.setHttpOnly(true);

        // Local development
        cookie.setSecure(false);

        cookie.setPath("/api/v1/auth");

        cookie.setMaxAge((int) (refreshTokenExpirationMs / 1000));

        response.addCookie(cookie);
    }


    // =========================================================
    // CLEAR REFRESH TOKEN COOKIE
    // =========================================================

    private void clearRefreshTokenCookie(HttpServletResponse response) {

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, null);

        cookie.setHttpOnly(true);

        cookie.setSecure(false);

        cookie.setPath("/api/v1/auth");

        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }

    public void forgetPassword(ForgetPasswordRequestDto request) {

        // 1. Request validation
        if (request == null || request.getUserName() == null || request.getUserName().trim().isEmpty()) {

            log.warn("Forget Password failed: Username, Email or Phone Number is missing");

            throw new IllegalArgumentException("Username, Email or Phone Number is required");
        }

        String providedValue = request.getUserName().trim();

        log.info("Forget Password request received for identifier={}", providedValue);

        // 2. Find user by Username / Email / Phone
        Optional<UserEntity> userOptional = userRepository.findByUserNameOrEmailOrPhone(providedValue, providedValue, providedValue);

        // 3. User not found
        if (userOptional.isEmpty()) {

            log.warn("Forget Password failed: No user found for identifier={}", providedValue);

            throw new RuntimeException("User Not Found with Requested Email, UserName or Phone Number");
        }

        UserEntity userEntity = userOptional.get();

        log.info("User found successfully. userId={}, userName={}", userEntity.getId(), userEntity.getUserName());

        // 4. Validate user's email
        if (userEntity.getEmail() == null || userEntity.getEmail().trim().isEmpty()) {

            log.error("Forget Password failed: Email not configured for userId={}", userEntity.getId());

            throw new RuntimeException("User Email is not configured");
        }

        // 5. Generate new password
        String newPassword = PasswordGenerator.generateRandomPassword();
        log.info("New password generated successfully for userId={}", userEntity.getId());

        // 6. Encrypt and save password
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userEntity.setFirstTime(true);

        userRepository.save(userEntity);

        log.info("New password saved successfully for userId={}", userEntity.getId());

        // 7. Prepare password reset email
        EmailRequestDto email = EmailRequestDto.builder().to(userEntity.getEmail()).subject(EmailSubjectConstant.PASSWORD_RESET_SUCCESSFUL).variables(Map.of("userName", userEntity.getName(), "providedValue", providedValue, "newPassword", newPassword)).template(EmailTemplateConstant.FORGET_PASSOWRD).build();

        // 8. Send email
        emailService.sendEmail(email);

        log.info("Password reset email sent successfully to userId={}, email={}", userEntity.getId(), userEntity.getEmail());

        log.info("Forget Password completed successfully for userId={}", userEntity.getId());
    }
}