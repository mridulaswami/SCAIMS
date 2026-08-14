package com.schoolerp.usermanagement.modules.role.requestdto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RoleRequestDto {

    @NotBlank(message = "Role name is required")
    private String roleName;

    @NotBlank(message = "Role description is required")
    private String roleDescription;

}
