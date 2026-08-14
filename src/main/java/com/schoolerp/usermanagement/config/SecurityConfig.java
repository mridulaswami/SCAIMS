package com.schoolerp.usermanagement.config;

import com.schoolerp.usermanagement.security.JwtAccessDeniedHandler;
import com.schoolerp.usermanagement.security.JwtAuthenticationEntryPoint;
import com.schoolerp.usermanagement.security.JwtAuthenticationFilter;
import com.schoolerp.usermanagement.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    // =========================================================
    // JWT FILTER
    // =========================================================

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {

        return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
    }

    // =========================================================
    // AUTHENTICATION ENTRY POINT
    // =========================================================

    @Bean
    public JwtAuthenticationEntryPoint authenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    // =========================================================
    // ACCESS DENIED HANDLER
    // =========================================================

    @Bean
    public JwtAccessDeniedHandler accessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF because this is REST API + JWT
                .csrf(csrf -> csrf.disable())

                // Exception handling
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()).accessDeniedHandler(accessDeniedHandler()))

                // Stateless JWT authentication
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization
                .authorizeHttpRequests(auth -> auth

                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()

                        // Authentication APIs
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Role APIs
                        .requestMatchers("/api/v1/roles/**").permitAll()

                        // Everything else requires JWT
                        .requestMatchers("/api/v1/**").authenticated()

                        .anyRequest().authenticated());

        // JWT Filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}