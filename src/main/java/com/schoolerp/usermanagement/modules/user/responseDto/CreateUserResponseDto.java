package com.schoolerp.usermanagement.modules.user.responseDto;

import com.schoolerp.usermanagement.modules.role.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponseDto {

    private UUID id;

    private String userName;

    private String name;

    private List<String> roleName;

    private String phoneNumber;

    private String email;

    private RoleEntity role;

    private Boolean status;

    private LocalDateTime createdAt;

    private String wardAssigned;
}

