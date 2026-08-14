package com.schoolerp.usermanagement.modules.auth.responseDto;

import com.schoolerp.usermanagement.modules.user.entity.UserEntity;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String accessToken;

    private String refreshToken;

    private String tokenType;

    private UserEntity user;

    private LocalDate accessTokenExpireAt;

    private LocalDate refreshTokenExpireAt;
}
