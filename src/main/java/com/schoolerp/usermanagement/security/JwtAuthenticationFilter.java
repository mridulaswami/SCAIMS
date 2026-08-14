package com.schoolerp.usermanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.Collections;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        try {

            // =========================================================
            // 1. Get JWT from Authorization header
            // =========================================================

            String jwt = getJwtFromRequest(request);

            /*
             * No JWT means this filter has nothing to authenticate.
             * Spring Security will decide later whether the endpoint
             * requires authentication or not.
             */
            if (!StringUtils.hasText(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            log.debug("JWT authentication started | method={} | uri={}", request.getMethod(), requestUri);

            // =========================================================
            // 2. Validate JWT signature + expiration
            // =========================================================

            if (!tokenProvider.validateToken(jwt)) {

                log.warn("JWT authentication failed - invalid or expired token | uri={}", requestUri);

                sendUnauthorizedResponse(request, response, "Your session has expired or the token is invalid. Please log in again.");

                return;
            }

            // =========================================================
            // 3. Make sure token is ACCESS token
            // =========================================================

            if (!tokenProvider.validateAccessToken(jwt)) {

                log.warn("JWT authentication failed - token is not an ACCESS token | uri={}", requestUri);

                sendUnauthorizedResponse(request, response, "Invalid access token.");

                return;
            }

            // =========================================================
            // 4. Get username from JWT
            // =========================================================

            String username = tokenProvider.getUsernameFromJWT(jwt);

            if (!StringUtils.hasText(username)) {

                log.warn("JWT authentication failed - username missing in token | uri={}", requestUri);

                sendUnauthorizedResponse(request, response, "Invalid access token.");

                return;
            }

            // =========================================================
            // 5. Load user from database
            // =========================================================

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails == null) {

                log.warn("JWT authentication failed - user details not found | username={}", username);

                sendUnauthorizedResponse(request, response, "User not found.");

                return;
            }

            // =========================================================
            // 6. Get claims
            // =========================================================

            Claims claims = tokenProvider.getClaims(jwt);

            // =========================================================
            // 7. Get role from JWT
            // =========================================================

            String role = claims.get("role", String.class);

            if (!StringUtils.hasText(role)) {

                log.warn("JWT authentication failed - role missing | username={}", username);

                sendUnauthorizedResponse(request, response, "Invalid access token. Role information is missing.");

                return;
            }

            // =========================================================
            // 8. Normalize role
            // =========================================================

            /*
             * JWT may contain:
             *
             * ADMIN
             * ROLE_ADMIN
             *
             * We normalize both to:
             *
             * ROLE_ADMIN
             */

            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }

            String authorityName = "ROLE_" + role;

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);

            // =========================================================
            // 9. Create Authentication
            // =========================================================

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.singletonList(authority));

            // =========================================================
            // 10. Set Security Context
            // =========================================================

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT authentication successful | username={} | role={} | authority={}", username, role, authorityName);

        } catch (UsernameNotFoundException ex) {

            log.warn("JWT authentication failed - user not found | uri={} | error={}", requestUri, ex.getMessage());

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(request, response, "User not found.");

            return;

        } catch (JwtException | IllegalArgumentException ex) {

            log.warn("JWT authentication failed | uri={} | error={}", requestUri, ex.getMessage());

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(request, response, "Your session has expired or the token is invalid. Please log in again.");

            return;

        } catch (Exception ex) {

            log.error("JWT authentication failed unexpectedly | uri={} | error={}", requestUri, ex.getMessage(), ex);

            SecurityContextHolder.clearContext();

            sendUnauthorizedResponse(request, response, "Authentication failed.");

            return;
        }

        // =========================================================
        // Continue request
        // =========================================================

        filterChain.doFilter(request, response);
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
    // Unauthorized Response
    // =============================================================

    private void sendUnauthorizedResponse(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", message, request.getRequestURI());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}