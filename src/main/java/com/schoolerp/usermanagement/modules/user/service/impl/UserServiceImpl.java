package com.schoolerp.usermanagement.modules.user.service.impl;

import com.schoolerp.usermanagement.common.response.PaginationResponse;
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
import com.schoolerp.usermanagement.modules.user.requestDto.CreateFieldEngineerDto;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateUserRequestDto;
import com.schoolerp.usermanagement.modules.user.requestDto.SendOptRequestDto;
import com.schoolerp.usermanagement.modules.user.responseDto.CreateUserResponseDto;
import com.schoolerp.usermanagement.modules.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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


    // =========================================================
    // REGISTER
    // =========================================================

    @Override
    @Transactional
    public void register(SendOptRequestDto requestDto) {

        log.info("Registration started | email={}", requestDto.getEmail());

        // Existing user check
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Generate OTP
        String otp = generateOtp();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(optExpiredTime);

        // Check existing OTP
        Optional<OtpEntity> existingOtp = otpEntityRepository.findTopByEmailOrderByCreatedAtDesc(requestDto.getEmail());

        OtpEntity otpEntity;

        if (existingOtp.isPresent()) {

            otpEntity = existingOtp.get();
            otpEntity.setOtp(otp);
            otpEntity.setExpiresAt(expiresAt);

        } else {

            otpEntity = OtpEntity.builder().email(requestDto.getEmail()).otp(otp).expiresAt(expiresAt).build();
        }

        otpEntityRepository.save(otpEntity);

        EmailRequestDto emailRequest = EmailRequestDto.builder().to(requestDto.getEmail()).subject(EmailSubjectConstant.REGISTRATION_OPT_SUBJECT).template(EmailTemplateConstant.REGISTRATION_OTP).variables(Map.of("name", requestDto.getName(),

                "otp", otp,

                "expiryMinutes", optExpiredTime)).build();

        emailService.sendEmail(emailRequest);

        log.info("Registration OTP sent | email={}", requestDto.getEmail());
    }


    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }


    // =========================================================
    // VERIFY OTP
    // NORMAL REGISTRATION
    //
    // IMPORTANT:
    // roleId remains INTEGER here.
    // Only one role is assigned.
    // Default role = CITIZEN (2)
    // =========================================================

    @Override
    @Transactional
    public CreateUserResponseDto verifyOtp(CreateUserRequestDto requestDto) {

        log.info("OTP verification started | email={}", requestDto.getEmail());

        // 1. Find latest OTP
        OtpEntity otpEntity = otpEntityRepository.findTopByEmailOrderByCreatedAtDesc(requestDto.getEmail()).orElseThrow(() -> new RuntimeException("OTP not found"));

        // 2. Check OTP expiry
        if (LocalDateTime.now().isAfter(otpEntity.getExpiresAt())) {

            throw new RuntimeException("OTP has expired");
        }

        // 3. Validate OTP
        if (!otpEntity.getOtp().equals(String.valueOf(requestDto.getOtp()))) {

            throw new RuntimeException("Invalid OTP");
        }

        // 4. Username check
        if (userRepository.existsByUserName(requestDto.getUserName())) {

            throw new RuntimeException("Username already exists");
        }

        // 5. Email check
        if (userRepository.existsByEmail(requestDto.getEmail())) {

            throw new RuntimeException("Email already exists");
        }

        // 6. Phone check
        if (userRepository.existsByPhone(requestDto.getPhoneNumber())) {

            throw new RuntimeException("Phone number already exists");
        }


        // =====================================================
        // 7. NORMAL REGISTRATION = SINGLE ROLE
        // =====================================================

        Integer roleId = requestDto.getRoleId() != null ? requestDto.getRoleId() : 2;

        RoleEntity role = roleEntityRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));


        // =====================================================
        // 8. Generate password
        // =====================================================

        String password = PasswordGenerator.generateRandomPassword();

        String encodedPassword = passwordEncoder.encode(password);


        // =====================================================
        // 9. Create User
        // =====================================================

        UserEntity user = UserEntity.builder().userName(requestDto.getUserName()).name(requestDto.getName()).phone(requestDto.getPhoneNumber()).email(requestDto.getEmail()).password(encodedPassword).status(true).isFirstTime(true).build();

        UserEntity savedUser = userRepository.save(user);

        log.info("User created after OTP verification | userId={} | email={}", savedUser.getId(), savedUser.getEmail());


        // =====================================================
        // 10. Save SINGLE role
        // =====================================================

        UserRoleEntity userRole = new UserRoleEntity();

        userRole.setUser(savedUser);
        userRole.setRole(role);

        userRoleRepository.save(userRole);


        // =====================================================
        // 11. Delete OTP
        // =====================================================

        otpEntityRepository.delete(otpEntity);


        // =====================================================
        // 12. Registration email
        // =====================================================

        EmailRequestDto emailRequest = EmailRequestDto.builder().to(savedUser.getEmail()).subject(EmailSubjectConstant.USER_REGISTRATION_SUCCESS).template(EmailTemplateConstant.USER_REGISTRATION_SUCCESS).variables(Map.of("name", savedUser.getName(),

                "email", savedUser.getEmail(),

                "userName", savedUser.getUserName(),

                "role", role.getRoleName(),

                "password", password)).build();

        emailService.sendEmail(emailRequest);


        // =====================================================
        // 13. Response
        //
        // Response is ALWAYS List<RoleEntity>
        // =====================================================

        return CreateUserResponseDto.builder().id(savedUser.getId()).userName(savedUser.getUserName()).name(savedUser.getName()).phoneNumber(savedUser.getPhone()).email(savedUser.getEmail()).roles(List.of(role)).build();
    }


    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequestDto request) {

        log.info("Password change started | username={}", request.getUserName());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {

            throw new RuntimeException("New password and confirm password do not match");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {

            throw new RuntimeException("New password must be different from current password");
        }

        UserEntity user = userRepository.findByUserName(request.getUserName()).orElseThrow(() -> {

            log.warn("Password change failed | User not found | username={}", request.getUserName());

            return new RuntimeException("User not found");
        });

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {

            log.warn("Password change failed | Invalid current password | username={}", request.getUserName());

            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setFirstTime(false);

        userRepository.save(user);

        log.info("Password changed and user activated successfully | username={}", request.getUserName());
    }


    // =========================================================
    // SEARCH USERS
    // RESPONSE = LIST OF ROLES
    // =========================================================

    @Override
    public Page<CreateUserResponseDto> searchUsers(String search, Pageable pageable) {

        try {

            Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

            Page<UserEntity> users = userRepository.searchByNameOrEmailOrPhone(search, unsortedPageable);

            log.info("searched users {}", users);

            List<CreateUserResponseDto> dtoList = users.stream().map(this::mapUserToResponse).collect(Collectors.toList());

            return new PageImpl<>(dtoList, unsortedPageable, users.getTotalElements());

        } catch (Exception e) {

            log.error("Error while searching users", e);

            throw new RuntimeException("Error while getting users from database", e);
        }
    }


    // =========================================================
    // REGISTER FIELD ENGINEER
    //
    // ONLY THIS API SUPPORTS MULTIPLE ROLES
    // =========================================================

    @Override
    @Transactional
    public CreateUserResponseDto registerFieldEngineer(CreateFieldEngineerDto request) {

        // Username
        if (userRepository.existsByUserName(request.getUserName())) {

            throw new RuntimeException("Username already exists");
        }

        // Email
        if (userRepository.existsByEmail(request.getEmail())) {

            throw new RuntimeException("Email already exists");
        }

        // Phone
        if (userRepository.existsByPhone(request.getPhoneNumber())) {

            throw new RuntimeException("Phone number already exists");
        }


        // =====================================================
        // MULTIPLE ROLE IDS
        // =====================================================

        List<Integer> roleIds = request.getRoleId();

        if (roleIds == null || roleIds.isEmpty()) {

            throw new RuntimeException("At least one role is required");
        }

        // Remove null + duplicates
        roleIds = roleIds.stream().filter(Objects::nonNull).distinct().toList();

        if (roleIds.isEmpty()) {

            throw new RuntimeException("At least one valid role is required");
        }


        // =====================================================
        // FIND ALL ROLES
        // =====================================================

        List<RoleEntity> roles = roleEntityRepository.findAllById(roleIds);

        if (roles.size() != roleIds.size()) {

            throw new RuntimeException("One or more roles not found");
        }


        // =====================================================
        // Generate password
        // =====================================================

        String password = PasswordGenerator.generateRandomPassword();

        String encodedPassword = passwordEncoder.encode(password);


        // =====================================================
        // Create user
        // =====================================================

        UserEntity user = UserEntity.builder().userName(request.getUserName()).name(request.getName()).phone(request.getPhoneNumber()).email(request.getEmail()).password(encodedPassword).status(true).isFirstTime(true).build();

        UserEntity savedUser = userRepository.save(user);


        // =====================================================
        // SAVE MULTIPLE ROLES
        // =====================================================

        List<UserRoleEntity> userRoles = roles.stream().map(role -> {

            UserRoleEntity userRole = new UserRoleEntity();

            userRole.setUser(savedUser);
            userRole.setRole(role);

            return userRole;
        }).toList();

        userRoleRepository.saveAll(userRoles);


        // =====================================================
        // ROLE NAMES FOR EMAIL
        // =====================================================

        String roleNames = roles.stream().map(RoleEntity::getRoleName).collect(Collectors.joining(", "));


        // =====================================================
        // REGISTRATION EMAIL
        // =====================================================

        EmailRequestDto emailRequest = EmailRequestDto.builder().to(savedUser.getEmail()).subject(EmailSubjectConstant.USER_REGISTRATION_SUCCESS).template(EmailTemplateConstant.USER_REGISTRATION_SUCCESS).variables(Map.of("name", savedUser.getName(),

                "email", savedUser.getEmail(),

                "userName", savedUser.getUserName(),

                "role", roleNames,

                "password", password)).build();

        emailService.sendEmail(emailRequest);


        log.info("Registration success email triggered | userId={} | email={} | roles={}", savedUser.getId(), savedUser.getEmail(), roleNames);


        // =====================================================
        // RESPONSE = LIST OF ROLES
        // =====================================================

        return CreateUserResponseDto.builder().id(savedUser.getId()).userName(savedUser.getUserName()).name(savedUser.getName()).phoneNumber(savedUser.getPhone()).email(savedUser.getEmail()).roles(roles).build();
    }


    // =========================================================
    // GET USERS
    // RESPONSE = LIST OF ROLES
    // =========================================================

    @Override
    public PaginationResponse<List<CreateUserResponseDto>> getUsers(int page, int size, Integer roleId, String search) {

        try {

            log.info("Started getting users | page={} | size={} | roleId={} | search={}", page, size, roleId, search);

            Pageable pageable = PageRequest.of(page, size);

            Page<UserEntity> users = userRepository.findUsersFiltered(roleId, search, pageable);

            log.info("Users fetched successfully | page={} | size={} | totalElements={}", users.getNumber(), users.getSize(), users.getTotalElements());

            List<CreateUserResponseDto> response = users.getContent().stream().map(this::mapUserToResponse).toList();

            return new PaginationResponse<>(response, users.getTotalElements(), users.getNumber(), users.getSize());

        } catch (Exception e) {

            log.error("Error while getting users", e);

            throw new RuntimeException("Error while getting users from database", e);
        }
    }

    // =========================================================
    // GET USER BY ID
    // RESPONSE = LIST OF ROLE NAMES
    // =========================================================

    @Override
    public CreateUserResponseDto getUserById(UUID id) {

        try {

            Optional<UserEntity> userData = userRepository.findById(id);

            List<RoleEntity> roles = getRolesByUser(userData.get());

            UserEntity user = userData.orElseThrow(() -> new RuntimeException("User not found"));

            return CreateUserResponseDto.builder().id(user.getId()).userName(user.getUserName()).roles(roles).status(user.isStatus()).createdAt(user.getCreatedAt()).phoneNumber(user.getPhone()).name(user.getName()).email(user.getEmail()).build();

        } catch (Exception e) {

            log.error("Error while getting user : {}", e.getMessage(), e);

            throw new RuntimeException("Error while getting user " + e.getMessage());
        }
    }


    // =========================================================
    // UPDATE PASSWORD
    // =========================================================

    @Override
    public CreateUserResponseDto updatePasswordById(UUID id, CreateUserRequestDto request) {

        try {

            UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

            String encodedPassword = passwordEncoder.encode(request.getUserName());

            user.setPassword(encodedPassword);

            UserEntity savedUser = userRepository.save(user);

            log.info("User Password Updated successfully : {}", savedUser.getId());

            return CreateUserResponseDto.builder().userName(savedUser.getUserName()).email(savedUser.getEmail()).build();

        } catch (RuntimeException ex) {

            log.error("Error while updating User Password : {}", ex.getMessage());

            throw new RuntimeException("Error while updating User Password " + ex.getMessage());
        }
    }


    // =========================================================
    // UPDATE USER
    // =========================================================

    @Override
    @Transactional
    public CreateUserResponseDto updateUserById(UUID id, CreateFieldEngineerDto updateRequest) {

        try {

            // ==========================================
            // 1. Find User
            // ==========================================
            UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));


            // ==========================================
            // 2. Update User Details
            // ==========================================
            user.setUserName(updateRequest.getUserName());
            user.setName(updateRequest.getName());
            user.setEmail(updateRequest.getEmail());
            user.setStatus(updateRequest.getStatus());

            UserEntity updatedUser = userRepository.save(user);


            // ==========================================
            // 3. Validate Role IDs
            // ==========================================
            List<Integer> requestedRoleIds = updateRequest.getRoleId();

            if (requestedRoleIds == null || requestedRoleIds.isEmpty()) {
                throw new RuntimeException("At least one role is required");
            }


            // Remove duplicate role IDs
            List<Integer> uniqueRoleIds = requestedRoleIds.stream().distinct().toList();


            // ==========================================
            // 4. Get Requested Roles From DB
            // ==========================================
            List<RoleEntity> requestedRoles = roleEntityRepository.findAllById(uniqueRoleIds);

            if (requestedRoles.size() != uniqueRoleIds.size()) {
                throw new RuntimeException("One or more roles not found");
            }


            // ==========================================
            // 5. Get Existing User Roles
            // ==========================================
            List<UserRoleEntity> existingUserRoles = userRoleRepository.findAllByUser(user);


            // ==========================================
            // 6. Delete Roles Which Are Not Requested
            // ==========================================
            List<UserRoleEntity> rolesToDelete = existingUserRoles.stream().filter(userRole -> !uniqueRoleIds.contains(userRole.getRole().getId())).toList();

            if (!rolesToDelete.isEmpty()) {
                userRoleRepository.deleteAll(rolesToDelete);
            }


            // ==========================================
            // 7. Find Existing Roles Which Should Stay
            // ==========================================
            Set<Integer> existingRoleIds = existingUserRoles.stream().map(userRole -> userRole.getRole().getId()).filter(uniqueRoleIds::contains).collect(Collectors.toSet());


            // ==========================================
            // 8. Add New Roles
            // ==========================================
            List<UserRoleEntity> rolesToAdd = requestedRoles.stream().filter(role -> !existingRoleIds.contains(role.getId())).map(role -> {

                UserRoleEntity userRole = new UserRoleEntity();

                userRole.setUser(user);
                userRole.setRole(role);

                return userRole;
            }).toList();

            if (!rolesToAdd.isEmpty()) {
                userRoleRepository.saveAll(rolesToAdd);
            }


            // ==========================================
            // 9. Get Final Roles From DB
            // ==========================================
            List<RoleEntity> finalRoles = userRoleRepository.findAllByUser(user).stream().map(UserRoleEntity::getRole).filter(Objects::nonNull).toList();


            // ==========================================
            // 10. Log
            // ==========================================
            log.info("User updated successfully | userId={} | roles={}", updatedUser.getId(), uniqueRoleIds);


            // ==========================================
            // 11. Response
            // ==========================================
            return CreateUserResponseDto.builder().id(updatedUser.getId()).userName(updatedUser.getUserName()).name(updatedUser.getName()).email(updatedUser.getEmail()).phoneNumber(updatedUser.getPhone()).status(updatedUser.isStatus()).roles(finalRoles).build();


        } catch (Exception e) {

            log.error("Error while updating User : {}", e.getMessage(), e);

            throw new RuntimeException("Error while updating User " + e.getMessage());
        }
    }

    // =========================================================
    // DELETE USER
    // =========================================================

    @Override
    public void deleteUserById(UUID id) {

        try {

            userRepository.deleteById(id);

        } catch (Exception e) {

            log.error("Error while deleting role : {}", e.getMessage(), e);

            throw new RuntimeException("Error while deleting role " + e.getMessage());
        }
    }


    // =========================================================
    // GET USERS BY ROLE
    // RESPONSE = LIST OF ROLES
    // =========================================================

    @Override
    public Page<CreateUserResponseDto> getUserByRole(Integer roleId, Pageable pageable) {

        try {

            Page<UserRoleEntity> usersByRoleId = userRoleRepository.findByRoleId(roleId, pageable);

            return usersByRoleId.map(userRole -> {

                UserEntity user = userRole.getUser();

                // Get ALL roles of this user
                List<RoleEntity> roles = getRolesByUser(user);

                return CreateUserResponseDto.builder().id(user.getId()).userName(user.getUserName()).roles(roles).name(user.getName()).phoneNumber(user.getPhone()).email(user.getEmail()).status(user.isStatus()).build();
            });

        } catch (Exception e) {

            log.error("Error while getting users by role", e);

            throw new RuntimeException("Error while getting users from database", e);
        }
    }


    // =========================================================
    // COMMON METHOD
    // GET ALL ROLES OF USER
    // =========================================================

    private List<RoleEntity> getRolesByUser(UserEntity user) {

        return userRoleRepository.findAllByUser(user).stream().map(UserRoleEntity::getRole).filter(Objects::nonNull).toList();
    }


    // =========================================================
    // COMMON USER RESPONSE MAPPER
    // =========================================================

    private CreateUserResponseDto mapUserToResponse(UserEntity user) {

        List<RoleEntity> roles = getRolesByUser(user);

        return CreateUserResponseDto.builder().id(user.getId()).userName(user.getUserName()).name(user.getName()).email(user.getEmail()).phoneNumber(user.getPhone()).status(user.isStatus()).createdAt(user.getCreatedAt()).roles(roles).build();
    }
}