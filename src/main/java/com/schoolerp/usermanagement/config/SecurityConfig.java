package com.schoolerp.usermanagement.config;

import com.schoolerp.usermanagement.security.JwtAccessDeniedHandler;
import com.schoolerp.usermanagement.security.JwtAuthenticationEntryPoint;
import com.schoolerp.usermanagement.security.JwtAuthenticationFilter;
import com.schoolerp.usermanagement.security.JwtTokenProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
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

                // =================================================
                // CSRF
                // =================================================
                .csrf(csrf -> csrf.disable())

                // =================================================
                // CORS
                // Uses CorsConfigurationSource bean
                // =================================================
                .cors(Customizer.withDefaults())

                // =================================================
                // EXCEPTION HANDLING
                // =================================================
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()).accessDeniedHandler(accessDeniedHandler()))

                // =================================================
                // STATELESS SESSION
                // =================================================
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // =================================================
                // AUTHORIZATION
                // =================================================
                .authorizeHttpRequests(auth -> auth

                        // -----------------------------------------
                        // CORS Preflight
                        // -----------------------------------------
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // -----------------------------------------
                        // Swagger
                        // -----------------------------------------
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // -----------------------------------------
                        // Actuator
                        // -----------------------------------------
                        .requestMatchers("/actuator/**").permitAll()

                        // -----------------------------------------
                        // Authentication APIs
                        // -----------------------------------------
                        .requestMatchers("/api/v1/auth/login").permitAll()

                        // -----------------------------------------
                        // Authentication APIs
                        // -----------------------------------------
                        .requestMatchers("/api/v1/auth/refresh").permitAll()

                        // -----------------------------------------
                        // User Create API
                        // -----------------------------------------
                        .requestMatchers("/api/v1/users").permitAll()

                        // -----------------------------------------
                        // Role APIs
                        // -----------------------------------------
                        .requestMatchers("/api/v1/roles/**").permitAll()

                        // -----------------------------------------
                        // All other API requires JWT
                        // -----------------------------------------
                        .requestMatchers("/api/v1/**").authenticated()

                        // -----------------------------------------
                        // Everything else
                        // -----------------------------------------
                        .anyRequest().authenticated())

                // =================================================
                // JWT FILTER
                // =================================================
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}