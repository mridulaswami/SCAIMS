package com.schoolerp.usermanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwt.secret:change-me-secret-key}")
    private String jwtSecret;

    /**
     * Access Token Expiration
     * Default: 15 minutes
     */
    @Value("${app.jwt.expiration-ms:900000}")
    private long accessTokenExpirationMs;

    /**
     * Refresh Token Expiration
     * Default: 7 days
     */
    @Value("${app.jwt.refresh-token-expiration-ms:604800000}")
    private long refreshTokenExpirationMs;

    private Key signingKey;

    @PostConstruct
    public void init() {

        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {

            log.warn("JWT secret is shorter than 32 bytes. " + "Provide a 32+ byte secret in production.");

            keyBytes = Arrays.copyOf(keyBytes, 32);
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        log.info("JWT signing key initialized successfully");
    }

    /**
     * Generate Access Token
     */
    public String generateAccessToken(String username, UUID id, String role) {

        Date now = new Date();

        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder().setSubject(username).claim("tokenType", "ACCESS").claim("userId", id).claim("role", role).setIssuedAt(now).setExpiration(expiryDate).signWith(signingKey, SignatureAlgorithm.HS256).compact();
    }

    /**
     * Generate Refresh Token
     */
    public String generateRefreshToken(String username) {

        Date now = new Date();

        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationMs);

        return Jwts.builder().setSubject(username).claim("tokenType", "REFRESH").setIssuedAt(now).setExpiration(expiryDate).signWith(signingKey, SignatureAlgorithm.HS256).compact();
    }

    /**
     * Extract username
     */
    public String getUsernameFromJWT(String token) {

        token = removeBearerPrefix(token);

        Claims claims = Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();

        return claims.getSubject();
    }

    /**
     * Extract email
     */
    public String getEmailFromJWT(String token) {

        token = removeBearerPrefix(token);

        Claims claims = Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();

        return claims.get("email", String.class);
    }

    /**
     * Extract phone number
     */
    public String getPhoneNumberFromJWT(String token) {

        token = removeBearerPrefix(token);

        Claims claims = Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();

        return claims.get("phoneNumber", String.class);
    }

    /**
     * Extract UserId
     */
    public String getUserIdFromJWT(String token) {

        token = removeBearerPrefix(token);

        Claims claims = Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();

        return claims.get("userId", String.class);
    }

    /**
     * Extract UserId
     */
    public String getRoleFromJWT(String token) {

        token = removeBearerPrefix(token);

        Claims claims = Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();

        return claims.get("role", String.class);
    }

    /**
     * Validate JWT
     */
    public boolean validateToken(String authToken) {

        try {

            String token = removeBearerPrefix(authToken);

            Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(token);

            return true;

        } catch (JwtException | IllegalArgumentException ex) {

            log.debug("Invalid JWT token | reason={}", ex.getMessage());

            return false;
        }
    }

    /**
     * Validate that token is an Access Token
     */
    public boolean validateAccessToken(String token) {

        try {

            token = removeBearerPrefix(token);

            Claims claims = getClaims(token);

            String tokenType = claims.get("tokenType", String.class);

            return "ACCESS".equals(tokenType);

        } catch (JwtException | IllegalArgumentException ex) {

            log.debug("Invalid access token | reason={}", ex.getMessage());

            return false;
        }
    }

    /**
     * Validate that token is a Refresh Token
     */
    public boolean validateRefreshToken(String token) {

        try {

            token = removeBearerPrefix(token);

            Claims claims = getClaims(token);

            String tokenType = claims.get("tokenType", String.class);

            return "REFRESH".equals(tokenType);

        } catch (JwtException | IllegalArgumentException ex) {

            log.debug("Invalid refresh token | reason={}", ex.getMessage());

            return false;
        }
    }

    /**
     * Get token type
     */
    public String getTokenType(String token) {

        token = removeBearerPrefix(token);

        Claims claims = getClaims(token);

        return claims.get("tokenType", String.class);
    }

    /**
     * Get all claims
     */
    public Claims getClaims(String jwt) {

        jwt = removeBearerPrefix(jwt);

        return Jwts.parser().setSigningKey(signingKey).build().parseClaimsJws(jwt).getBody();
    }

    /**
     * Remove Bearer prefix
     */
    private String removeBearerPrefix(String token) {

        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }
}