package com.schoolerp.usermanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolerp.usermanagement.common.response.ErrorResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService, ObjectMapper objectMapper) {

        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    // =============================================================
    // Public endpoints - JWT filter will be skipped
    // =============================================================

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String path = request.getServletPath();

        return path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/refresh") || path.equals("/api/v1/auth/forgetPassword") || path.equals("/api/v1/users/register") || path.equals("/api/v1/users/verify") || path.equals("/api/v1/users/changePassword") || path.startsWith("/api/v1/roles/") || path.startsWith("/swagger-ui/") || path.equals("/swagger-ui.html") || path.startsWith("/v3/api-docs/") || path.startsWith("/uploads/");
    }

    // =============================================================
    // JWT Authentication
    // =============================================================

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        String jwt = getJwtFromRequest(request);

        // No token
        if (!StringUtils.hasText(jwt)) {

            // Let Spring Security handle missing token
            filterChain.doFilter(request, response);
            return;
        }

        try {

            log.debug("JWT authentication started | method={} | uri={}", request.getMethod(), requestUri);

            // =====================================================
            // 1. Validate JWT
            // =====================================================

            if (!tokenProvider.validateToken(jwt)) {

                log.warn("JWT authentication failed - invalid or expired token | uri={}", requestUri);

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(response, "Invalid or expired token");

                return;
            }

            // =====================================================
            // 2. Validate ACCESS token
            // =====================================================

            if (!tokenProvider.validateAccessToken(jwt)) {

                log.warn("JWT authentication failed - token is not ACCESS token | uri={}", requestUri);

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(response, "Invalid access token");

                return;
            }

            // =====================================================
            // 3. Get username
            // =====================================================

            String username = tokenProvider.getUsernameFromJWT(jwt);

            if (!StringUtils.hasText(username)) {

                log.warn("JWT authentication failed - username missing | uri={}", requestUri);

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(response, "Invalid token: username is missing");

                return;
            }

            // =====================================================
            // 4. Load user
            // =====================================================

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails == null) {

                log.warn("JWT authentication failed - user not found | username={}", username);

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(response, "User associated with token not found");

                return;
            }

            // =====================================================
            // 5. Get claims
            // =====================================================

            Claims claims = tokenProvider.getClaims(jwt);

            // =====================================================
            // 6. Get roles from JWT
            // =====================================================

            List<String> roles = extractRoles(claims);

            if (roles.isEmpty()) {

                log.warn("JWT authentication failed - roles missing | username={}", username);

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(response, "Invalid token: role is missing");

                return;
            }

            // =====================================================
            // 7. Create authorities
            // =====================================================

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            for (String role : roles) {

                if (!StringUtils.hasText(role)) {
                    continue;
                }

                role = role.trim();

                if (role.startsWith("ROLE_")) {
                    role = role.substring(5);
                }

                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }

            if (authorities.isEmpty()) {

                SecurityContextHolder.clearContext();

                sendUnauthorizedResponse(response, "Invalid token: no valid role found");

                return;
            }

            // =====================================================
            // 8. Create Authentication
            // =====================================================

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            // =====================================================
            // 9. Set Security Context
            // =====================================================

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authentication successful | username={} | roles={}", username, roles);

            // Continue request
            filterChain.doFilter(request, response);

        } catch (UsernameNotFoundException ex) {

            log.warn("JWT authentication failed - user not found | uri={} | error={}", requestUri, ex.getMessage());

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(response, "User associated with token not found");

        } catch (JwtException | IllegalArgumentException ex) {

            log.warn("JWT authentication failed | uri={} | error={}", requestUri, ex.getMessage());

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(response, "Invalid token");

        } catch (Exception ex) {

            log.error("JWT authentication failed unexpectedly | uri={} | error={}", requestUri, ex.getMessage(), ex);

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(response, "Authentication failed: " + ex.getMessage());
        }
    }

    // =============================================================
    // Extract roles from JWT
    // =============================================================

    private List<String> extractRoles(Claims claims) {

        Object roleClaim = claims.get("role");

        if (roleClaim == null) {
            return Collections.emptyList();
        }

        // ---------------------------------------------------------
        // Case 1: role is String
        // ---------------------------------------------------------

        if (roleClaim instanceof String role) {

            return Collections.singletonList(role);
        }

        // ---------------------------------------------------------
        // Case 2: role is List
        // ---------------------------------------------------------

        if (roleClaim instanceof Collection<?> collection) {

            return collection.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        }

        return Collections.emptyList();
    }

    // =============================================================
    // Get JWT from Authorization header
    // =============================================================

    private String getJwtFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {

            return bearerToken.substring(7).trim();
        }

        return null;
    }

    // =============================================================
// Send JWT Unauthorized Response
// =============================================================

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new LinkedHashMap<>();

        errorResponse.put("timestamp", Instant.now());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}