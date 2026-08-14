package com.schoolerp.usermanagement.modules.auth.service.impl;

import com.schoolerp.usermanagement.modules.auth.requestDto.LoginRequestDto;
import com.schoolerp.usermanagement.modules.auth.responseDto.LoginResponseDto;
import com.schoolerp.usermanagement.modules.auth.service.AuthService;
import com.schoolerp.usermanagement.modules.auth.repository.AuthEntityRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserEntityRepository userRepository;
    private final AuthEntityRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration-ms:900000}")
    private long accessTokenExpirationMs;

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

            // 1. Authenticate username + password
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestDto.getUserName(), requestDto.getPassword()));

            log.debug("User authentication successful | username={}", requestDto.getUserName());


            // 2. Find User
            UserEntity user = userRepository.findByUserName(requestDto.getUserName()).orElseThrow(() -> {

                log.warn("Login failed - user not found | username={}", requestDto.getUserName());

                return new RuntimeException("User not found");
            });


            // 3. Check User Status
            if (!user.isStatus()) {

                log.warn("Login rejected - user inactive | userId={} | username={}", user.getId(), user.getUserName());

                throw new RuntimeException("User account is inactive");
            }


            // 4. Get Role
            String role = user.getRoleId().getRoleName();

            log.debug("User role loaded | userId={} | username={} | role={}", user.getId(), user.getUserName(), role);


            // 5. Generate Access Token
            String accessToken = jwtTokenProvider.generateAccessToken(user.getUserName(), role);


            // 6. Generate Refresh Token
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserName());


            // 7. Store Refresh Token
            // Keep your existing AuthEntity save logic here
            // if you are storing refresh token in auths table.


            // 8. Add Refresh Token Cookie
            addRefreshTokenCookie(response, refreshToken);


            log.info("Login successful | userId={} | username={} | role={}", user.getId(), user.getUserName(), role);


            // 9. Build Response
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
    @Transactional(readOnly = true)
    public LoginResponseDto refreshToken(String refreshToken) {

        log.info("Refresh token request received");

        try {

            if (refreshToken == null || refreshToken.isBlank()) {

                log.warn("Refresh token rejected - token missing");

                throw new RuntimeException("Refresh token is required");
            }


            // 1. Validate JWT
            if (!jwtTokenProvider.validateToken(refreshToken)) {

                log.warn("Refresh token validation failed");

                throw new RuntimeException("Invalid refresh token");
            }


            // 2. Check Token Type
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {

                log.warn("Invalid token type - expected REFRESH");

                throw new RuntimeException("Invalid refresh token type");
            }


            // 3. Extract Username
            String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);


            // 4. Find User
            UserEntity user = userRepository.findByUserName(username).orElseThrow(() -> {

                log.warn("Refresh failed - user not found | username={}", username);

                return new RuntimeException("User not found");
            });


            // 5. Check User Status
            if (!user.isStatus()) {

                log.warn("Refresh rejected - user inactive | userId={} | username={}", user.getId(), user.getUserName());

                throw new RuntimeException("User account is inactive");
            }


            // 6. Get Role
            String role = user.getRoleId().getRoleName();


            // 7. Generate New Access Token
            String accessToken = jwtTokenProvider.generateAccessToken(user.getUserName(), role);


            log.info("Access token refreshed successfully | userId={} | username={} | role={}", user.getId(), user.getUserName(), role);


            // 8. Return Response
            return LoginResponseDto.builder().accessToken(accessToken).tokenType("Bearer").user(user).build();

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

            if (accessToken == null || accessToken.isBlank()) {

                log.warn("Logout failed - access token missing");

                throw new RuntimeException("Access token is required");
            }


            // Remove Bearer prefix
            if (accessToken.startsWith("Bearer ")) {

                accessToken = accessToken.substring(7);
            }


            // 1. Validate Access Token
            if (!jwtTokenProvider.validateToken(accessToken)) {

                log.warn("Logout failed - invalid access token");

                throw new RuntimeException("Invalid access token");
            }


            // 2. Make sure it is ACCESS token
            if (!jwtTokenProvider.validateAccessToken(accessToken)) {

                log.warn("Logout failed - token is not ACCESS token");

                throw new RuntimeException("Invalid access token type");
            }


            // 3. Get Username
            String username = jwtTokenProvider.getUsernameFromJWT(accessToken);


            // 4. Find User
            UserEntity user = userRepository.findByUserName(username).orElseThrow(() -> {

                log.warn("Logout failed - user not found | username={}", username);

                return new RuntimeException("User not found");
            });


            // 5. Delete Auth / Refresh Token Entry
            authRepository.deleteByUserId(user.getId());


            // 6. Clear Refresh Token Cookie
            clearRefreshTokenCookie(response);


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
}