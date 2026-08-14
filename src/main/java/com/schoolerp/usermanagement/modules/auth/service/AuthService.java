package com.schoolerp.usermanagement.modules.auth.service;

import com.schoolerp.usermanagement.modules.auth.requestDto.LoginRequestDto;
import com.schoolerp.usermanagement.modules.auth.responseDto.LoginResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    LoginResponseDto login(LoginRequestDto requestDto, HttpServletResponse response);

    LoginResponseDto refreshToken(String refreshToken);

    void logout(String accessToken, HttpServletResponse response);
}
