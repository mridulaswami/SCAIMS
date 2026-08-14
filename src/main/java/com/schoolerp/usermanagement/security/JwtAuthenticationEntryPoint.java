package com.schoolerp.usermanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final com.fasterxml.jackson.databind.ObjectMapper mapper;

    public JwtAuthenticationEntryPoint() {
        this.mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        this.mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        String authHeader = request.getHeader("Authorization");
        String message = "Authentication failed";
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            message = "Token is missing. Please provide a valid Bearer token.";
        } else {
            message = authException.getMessage() != null ? authException.getMessage() : "Authentication failed";
        }

        com.schoolerp.usermanagement.common.response.ErrorResponse errorResponse = new com.schoolerp.usermanagement.common.response.ErrorResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized",
                message,
                request.getRequestURI());

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
