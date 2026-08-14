package com.schoolerp.usermanagement.modules.user.service;

import com.schoolerp.usermanagement.modules.user.requestDto.CreateUserRequestDto;
import com.schoolerp.usermanagement.modules.user.responseDto.CreateUserResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public CreateUserResponseDto createUser(CreateUserRequestDto request);

}
