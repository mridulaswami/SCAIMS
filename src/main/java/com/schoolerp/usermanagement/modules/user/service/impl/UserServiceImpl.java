package com.schoolerp.usermanagement.modules.user.service.impl;

import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import com.schoolerp.usermanagement.modules.role.repository.RoleEntityRepository;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateUserRequestDto;
import com.schoolerp.usermanagement.modules.user.responseDto.CreateUserResponseDto;
import com.schoolerp.usermanagement.modules.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserEntityRepository userRepository;
    private final RoleEntityRepository roleEntityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public CreateUserResponseDto createUser(CreateUserRequestDto requestDto) {

        log.info("User creation request received | username={} | email={} | roleId={} | status={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId(), requestDto.isStatus());

        try {

            // 1. Find Role
            RoleEntity role = roleEntityRepository.findById(requestDto.getRoleId()).orElseThrow(() -> {

                log.warn("User creation failed - Role not found | roleId={}", requestDto.getRoleId());

                return new RuntimeException("Role not found");
            });

            log.debug("Role found successfully | roleId={} | roleName={}", role.getId(), role.getRoleName());

            // 2. Encrypt Password
            String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

            log.debug("User password encrypted successfully | username={}", requestDto.getUserName());

            // 3. Build User Entity
            UserEntity userRequest = UserEntity.builder().userName(requestDto.getUserName()).name(requestDto.getName()).email(requestDto.getEmail()).password(encodedPassword).roleId(role).status(requestDto.isStatus()).build();

            // 4. Save User
            UserEntity savedUser = userRepository.save(userRequest);

            log.info("User created successfully | userId={} | username={} | email={} | role={} | status={}", savedUser.getId(), savedUser.getUserName(), savedUser.getEmail(), role.getRoleName(), savedUser.isStatus());

            // 5. Build Response
            return CreateUserResponseDto.builder().id(savedUser.getId()).userName(savedUser.getUserName()).name(savedUser.getName()).email(savedUser.getEmail()).role(role).status(savedUser.isStatus()).createdAt(savedUser.getCreatedAt()).build();

        } catch (RuntimeException ex) {

            log.error("User creation failed | username={} | email={} | roleId={} | error={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId(), ex.getMessage(), ex);

            throw ex;
        }
    }
}