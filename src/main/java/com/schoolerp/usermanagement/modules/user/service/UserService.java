package com.schoolerp.usermanagement.modules.user.service;

import com.schoolerp.usermanagement.modules.role.requestdto.RoleRequestDto;
import com.schoolerp.usermanagement.modules.role.responsedto.RoleResponseDto;
import com.schoolerp.usermanagement.modules.user.requestDto.ChangePasswordRequestDto;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateFieldEngineerDto;
import com.schoolerp.usermanagement.modules.user.requestDto.CreateUserRequestDto;
import com.schoolerp.usermanagement.modules.user.requestDto.SendOptRequestDto;
import com.schoolerp.usermanagement.modules.user.responseDto.CreateUserResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface UserService {

    CreateUserResponseDto verifyOtp(CreateUserRequestDto request);

    void register(SendOptRequestDto requestDto);

    public CreateUserResponseDto registerFieldEngineer(CreateFieldEngineerDto request);

    public List<CreateUserResponseDto> getUsers();
    public CreateUserResponseDto getUserById(UUID id);

    public CreateUserResponseDto updatePasswordById(UUID id, CreateUserRequestDto request);
    public CreateUserResponseDto updateUserById( UUID id, CreateUserRequestDto roleRequestDto);

    public void deleteUserById( UUID id);
    public List<CreateUserResponseDto> getUserByRole(Integer id);

    void changePassword(ChangePasswordRequestDto request);

}
