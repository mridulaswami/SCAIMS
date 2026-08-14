package com.schoolerp.usermanagement.common.util;

import com.schoolerp.usermanagement.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtUtil {

    private final JwtTokenProvider tokenProvider;

    @Autowired
    public JwtUtil(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    public boolean validate(String token) {
        return token != null && tokenProvider.validateToken(token);
    }

    public String getUsername(String token) {
        return tokenProvider.getUsernameFromJWT(token);
    }
}
