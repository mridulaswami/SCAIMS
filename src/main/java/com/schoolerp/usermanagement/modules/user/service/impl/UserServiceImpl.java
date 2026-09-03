package com.schoolerp.usermanagement.modules.user.service.impl;

import com.schoolerp.usermanagement.common.util.PasswordGenerator;
import com.schoolerp.usermanagement.modules.auth.entity.OtpEntity;
import com.schoolerp.usermanagement.modules.auth.entity.UserRoleEntity;
import com.schoolerp.usermanagement.modules.auth.repository.OtpEntityRepository;
import com.schoolerp.usermanagement.modules.auth.repository.UserRoleRepository;
import com.schoolerp.usermanagement.modules.email.constant.EmailSubjectConstant;
import com.schoolerp.usermanagement.modules.email.constant.EmailTemplateConstant;
import com.schoolerp.usermanagement.modules.email.requestDto.EmailRequestDto;
import com.schoolerp.usermanagement.modules.email.service.EmailService;
import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import com.schoolerp.usermanagement.modules.role.repository.RoleEntityRepository;
import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import com.schoolerp.usermanagement.modules.user.repository.UserEntityRepository;
import com.schoolerp.usermanagement.modules.user.requestDto.ChangePasswordRequestDto;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateUserRequestDto;
import com.schoolerp.usermanagement.modules.user.requestDto.SendOptRequestDto;
import com.schoolerp.usermanagement.modules.user.responseDto.CreateUserResponseDto;
import com.schoolerp.usermanagement.modules.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
    private final OtpEntityRepository otpEntityRepository;

    @Value("${spring.opt.exprired-at}")
    private Integer optExpiredTime;


    @Override
    @Transactional
    public void register(SendOptRequestDto requestDto) {

        log.info("Registration started  | email={}", requestDto.getEmail());

        // 1. Existing user check
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 2. Generate OTP
        String otp = generateOtp();

        // 3. OTP expiry - 5 minutes
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(optExpiredTime);

        // 4. Check existing OTP for email
        Optional<OtpEntity> existingOtp = otpEntityRepository.findTopByEmailOrderByCreatedAtDesc(requestDto.getEmail());

        OtpEntity otpEntity;

        if (existingOtp.isPresent()) {

            // Existing OTP update
            otpEntity = existingOtp.get();
            otpEntity.setOtp(otp);
            otpEntity.setExpiresAt(expiresAt);

        } else {

            // New OTP
            otpEntity = OtpEntity.builder().email(requestDto.getEmail()).otp(otp).expiresAt(expiresAt).build();
        }

        // 5. Save OTP
        otpEntityRepository.save(otpEntity);

        // 6. Send OTP Email
        EmailRequestDto emailRequest = EmailRequestDto.builder().to(requestDto.getEmail()).subject(EmailSubjectConstant.REGISTRATION_OPT_SUBJECT).template(EmailTemplateConstant.REGISTRATION_OTP).variables(Map.of("name", requestDto.getName(), "otp", otp, "expiryMinutes", optExpiredTime)).build();

        emailService.sendEmail(emailRequest);

        log.info("Registration OTP sent | email={}", requestDto.getEmail());
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @Override
    @Transactional
    public CreateUserResponseDto verifyOtp(CreateUserRequestDto requestDto) {

        log.info("OTP verification started | email={}", requestDto.getEmail());

        // 1. Find latest OTP
        OtpEntity otpEntity = otpEntityRepository.findTopByEmailOrderByCreatedAtDesc(requestDto.getEmail()).orElseThrow(() -> new RuntimeException("OTP not found"));

        // 2. Check OTP expired
        if (LocalDateTime.now().isAfter(otpEntity.getExpiresAt())) {
            throw new RuntimeException("OTP has expired");
        }
        log.info(otpEntity.getOtp() + "  " + "Abhin" + requestDto.getOtp());
        // 3. Check OTP
        if (!otpEntity.getOtp().equals(String.valueOf(requestDto.getOtp()))) {
            throw new RuntimeException("Invalid OTP");
        }

        // 4. Check username
        if (userRepository.existsByUserName(requestDto.getUserName())) {
            throw new RuntimeException("Username already exists");
        }

        // 5. Check email
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 6. Check phone
        if (userRepository.existsByPhone(requestDto.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        // 7. Role
        Integer roleId = requestDto.getRoleId() != null ? requestDto.getRoleId() : 2;

        RoleEntity role = roleEntityRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));

        // 8. Encrypt password
        String password = PasswordGenerator.generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(password);

        // 9. Create User
        UserEntity user = UserEntity.builder().userName(requestDto.getUserName()).name(requestDto.getName()).phone(requestDto.getPhoneNumber()).email(requestDto.getEmail()).password(encodedPassword)

                // User created but NOT active
                .status(false)

                .build();

        UserEntity savedUser = userRepository.save(user);

        log.info("User created after OTP verification | userId={} | email={}", savedUser.getId(), savedUser.getEmail());

        // 10. User Role Mapping
        UserRoleEntity userRole = new UserRoleEntity();

        userRole.setUser(savedUser);
        userRole.setRole(role);

        userRoleRepository.save(userRole);

        // 11. Delete OTP after successful verification
        otpEntityRepository.delete(otpEntity);

        // 12. Registration Success Email
        EmailRequestDto emailRequest = EmailRequestDto.builder().to(savedUser.getEmail()).subject(EmailSubjectConstant.USER_REGISTRATION_SUCCESS).template(EmailTemplateConstant.USER_REGISTRATION_SUCCESS).variables(Map.of("name", savedUser.getName(), "email", savedUser.getEmail(), "userName", savedUser.getUserName(), "role", role.getRoleName(), "password", password)).build();

        emailService.sendEmail(emailRequest);

        log.info("Registration success email triggered | userId={} | email={}", savedUser.getId(), savedUser.getEmail());

        // 13. Response
        return CreateUserResponseDto.builder().id(savedUser.getId()).userName(savedUser.getUserName()).name(savedUser.getName()).phoneNumber(savedUser.getPhone()).email(savedUser.getEmail()).role(role).build();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDto request) {

        log.info("Password change started | username={}", request.getUserName());

        // New password and confirm password check
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Prevent same password
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Find user by username
        UserEntity user = userRepository.findByUserName(request.getUserName()).orElseThrow(() -> {
            log.warn("Password change failed | User not found | username={}", request.getUserName());
            return new RuntimeException("User not found");
        });

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed | Invalid current password | username={}", request.getUserName());

            throw new RuntimeException("Current password is incorrect");
        }

        // Encode and update new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Activate user after password change
        user.setStatus(true);

        userRepository.save(user);

        log.info("Password changed and user activated successfully | username={}", request.getUserName());
    }


    @Override
    public List<CreateUserResponseDto> getUsers() {
        try {
            List<UserEntity> users = userRepository.findAll();
            log.info("users", users);
            return users.stream().map(user -> {
                UserRoleEntity userRole = userRoleRepository.findByUser(user);

                return CreateUserResponseDto.builder().id(user.getId()).userName(user.getUserName()).name(user.getName()).email(user.getEmail()).phoneNumber(user.getPhone()).role(userRole != null ? userRole.getRole() : null).build();
            }).collect(Collectors.toList());
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

    public List<CreateUserResponseDto> getUserByRole(Integer roleId) {
        try {
            List<UserRoleEntity> usersByRoleId = userRoleRepository.findByRoleId(roleId);

            System.out.println(usersByRoleId);

            return usersByRoleId.stream().map(userRole -> {
                UserEntity user = userRole.getUser();
                UserRoleEntity userRoleEntity = userRoleRepository.findByUser(user);

                return CreateUserResponseDto.builder().id(user.getId()).userName(user.getUserName()).role(userRole.getRole()).name(user.getName()).phoneNumber(user.getPhone()).email(user.getEmail()).build();
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Error while getting users from database", e);
        }
    }

}