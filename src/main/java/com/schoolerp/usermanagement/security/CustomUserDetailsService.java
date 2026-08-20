package com.schoolerp.usermanagement.security;

import com.schoolerp.usermanagement.modules.auth.entity.UserRoleEntity;
import com.schoolerp.usermanagement.modules.auth.repository.UserRoleRepository;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserEntityRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        log.debug("Loading user for authentication | username={}", username);

        UserEntity user = userRepository.findByUserName (username).orElseThrow(() -> {

            log.warn("Authentication failed - user not found | username={}", username);

            return new UsernameNotFoundException("User not found: " + username);
        });

        // =====================================================
        // USER STATUS
        // =====================================================

        boolean enabled = user.isStatus();

        // =====================================================
        // ROLE
        // =====================================================

        UserRoleEntity userRole = userRoleRepository.findByUserId(user.getId()).stream().findFirst().orElseThrow(() -> new RuntimeException("Role not assigned to user"));
        String roleName = userRole.getRole().getRoleName();


        // Prevent ROLE_ROLE_ADMIN
        if (roleName.startsWith("ROLE_")) {
            roleName = roleName.substring(5);
        }

        String authorityName = "ROLE_" + roleName;

        log.debug("User loaded successfully | userId={} | username={} | role={} | enabled={}", user.getId(), user.getUserName(), roleName, enabled);

        // =====================================================
        // AUTHORITIES
        // =====================================================

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authorityName));

        // =====================================================
        // CUSTOM PRINCIPAL
        // =====================================================

        return new CustomUserPrincipal(user.getId().toString(), user.getUserName(), user.getPassword(), authorities, enabled);
    }
}