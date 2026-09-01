package com.schoolerp.usermanagement.modules.user.service.impl;

import com.schoolerp.usermanagement.modules.auth.entity.UserRoleEntity;
import com.schoolerp.usermanagement.modules.auth.repository.UserRoleRepository;
import com.schoolerp.usermanagement.modules.email.constant.EmailSubjectConstant;
import com.schoolerp.usermanagement.modules.email.constant.EmailTemplateConstant;
import com.schoolerp.usermanagement.modules.email.requestDto.EmailRequestDto;
import com.schoolerp.usermanagement.modules.email.service.EmailService;
import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import com.schoolerp.usermanagement.modules.role.repository.RoleEntityRepository;
import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.responsedto.RoleResponseDto;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserEntityRepository userRepository;
    private final RoleEntityRepository roleEntityRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public CreateUserResponseDto createUser(CreateUserRequestDto requestDto) {

        log.info("User creation request received | username={} | email={} | roleId={} | status={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId(), requestDto.isStatus());

        try {

            // 1. Check existing username
            if (userRepository.existsByUserName(requestDto.getUserName())) {
                throw new RuntimeException("Username already exists");
            }

            // 2. Check existing email
            if (userRepository.existsByEmail(requestDto.getEmail())) {
                throw new RuntimeException("Email already exists");
            }

            // 3. Check existing phone number
            if (userRepository.existsByPhone(requestDto.getPhoneNumber())) {
                throw new RuntimeException("Phone number already exists");
            }

            // 4. Find Role
            Integer roleId = requestDto.getRoleId() != null ? requestDto.getRoleId() : 2;

            RoleEntity role = roleEntityRepository.findById(roleId).orElseThrow(() -> {
                log.warn("User creation failed - Role not found | roleId={}", roleId);
                return new RuntimeException("Role not found");
            });

            log.debug("Role found successfully | roleId={} | roleName={}", role.getId(), role.getRoleName());

            // 5. Encrypt Password
            String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

            log.debug("User password encrypted successfully | username={}", requestDto.getUserName());

            // 6. Build User Entity
            UserEntity userRequest = UserEntity.builder().userName(requestDto.getUserName()).name(requestDto.getName()).phone(requestDto.getPhoneNumber()).email(requestDto.getEmail()).password(encodedPassword).status(requestDto.isStatus()).build();

            // 7. Save User
            UserEntity savedUser = userRepository.save(userRequest);

            log.info("User created successfully | userId={} | username={} | email={} | role={}", savedUser.getId(), savedUser.getUserName(), savedUser.getEmail(), role.getRoleName());

            // 8. Create User-Role mapping
            UserRoleEntity userRole = new UserRoleEntity();

            userRole.setUser(savedUser);
            userRole.setRole(role);

            UserRoleEntity savedUserRole = userRoleRepository.save(userRole);

            log.info("User-role mapping created successfully | userId={} | roleId={} | userRoleId={}", savedUser.getId(), role.getId(), savedUserRole.getId());

            // 9. Send Registration Success Email
            EmailRequestDto emailRequest = EmailRequestDto.builder().to(savedUser.getEmail()).subject(EmailSubjectConstant.USER_REGISTRATION_SUCCESS).template(EmailTemplateConstant.USER_REGISTRATION_SUCCESS).variables(Map.of("name", savedUser.getName(), "email", savedUser.getEmail(), "userName", savedUser.getUserName(), "role", role.getRoleName())).build();

            emailService.sendEmail(emailRequest);

            log.info("User registration email triggered | userId={} | email={}", savedUser.getId(), savedUser.getEmail());

            // 10. Build Response
            return CreateUserResponseDto.builder().id(savedUser.getId()).userName(savedUser.getUserName()).name(savedUser.getName()).phoneNumber(requestDto.getPhoneNumber()).email(savedUser.getEmail()).role(role).build();

        } catch (RuntimeException ex) {

            log.error("User creation failed | username={} | email={} | roleId={} | error={}", requestDto.getUserName(), requestDto.getEmail(), requestDto.getRoleId(), ex.getMessage(), ex);

            throw ex;
        }
    }

    @Override
    public List<CreateUserResponseDto> getUsers() {
        try {
            List<UserEntity> users = userRepository.findAll();
            log.info("users", users);
            return users.stream().map(user -> CreateUserResponseDto.builder().id(user.getId()).userName(user.getUserName()).name(user.getName()).email(user.getEmail()).build()).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error while getting users from database");
        }
    }

    @Override
    public CreateUserResponseDto getUserById(UUID id) {
        try {
            Optional<UserEntity> userData = userRepository.findById(id);
            UserEntity user = userData.orElseThrow(() -> new RuntimeException("Role not found"));
            return CreateUserResponseDto.builder().id(user.getId()).userName(user.getUserName()).name(user.getName()).email(user.getEmail()).build();
        } catch (Exception e) {
            log.error("Error while getting user : {}", e.getMessage());
            throw new RuntimeException("Error while getting user" + e.getMessage());
        }
    }

    @Override
    public CreateUserResponseDto updatePasswordById(UUID id, CreateUserRequestDto request) {
        try {
            UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Role not found"));
            String encodedPassword = passwordEncoder.encode(request.getUserName());

            user.setPassword(encodedPassword);
            UserEntity savedUser = userRepository.save(user);
            log.info("User Password Updated successfully : {}", savedUser.getId());
            return CreateUserResponseDto.builder().userName(savedUser.getUserName()).email(savedUser.getEmail()).build();
        } catch (RuntimeException ex) {
            log.error("Error while updating User Password : {}", ex.getMessage());
            throw new RuntimeException("Error while updating User Password" + ex.getMessage());
        }
    }


    @Override
    public CreateUserResponseDto updateUserById(UUID id, CreateUserRequestDto updateRequest) {
        try {

            UserEntity getUserData = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            getUserData.setUserName(updateRequest.getUserName());
            getUserData.setName(updateRequest.getName());
            getUserData.setEmail(updateRequest.getEmail());

            UserEntity updatedUser = userRepository.save(getUserData);
            log.info("User Updated successfully : {}", updatedUser);
            return CreateUserResponseDto.builder().id(updatedUser.getId()).userName(updatedUser.getUserName()).name(updatedUser.getName()).email(updatedUser.getEmail()).build();
        } catch (Exception e) {
            log.error("Error while updating User : {}", e.getMessage());
            throw new RuntimeException("Error while updating User" + e.getMessage());
        }


    }

    @Override
    public void deleteUserById(UUID id) {
        try {
            userRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error while deleting role : {}", e.getMessage());
            throw new RuntimeException("Error while deleting role" + e.getMessage());
        }
    }

    public List<CreateUserResponseDto> getUserByRole(UUID roleId) {
        try {
            List<UserRoleEntity> usersByRoleId = userRoleRepository.findByRoleId(roleId);

            System.out.println(usersByRoleId);

            return usersByRoleId.stream().map(userRole -> {
                UserEntity user = userRole.getUser();

                return CreateUserResponseDto.builder().id(user.getId()).userName(user.getUserName()).name(user.getName()).email(user.getEmail()).build();
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error while getting users from database", e);
        }
    }

}