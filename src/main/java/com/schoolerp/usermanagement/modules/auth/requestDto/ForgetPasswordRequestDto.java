package com.schoolerp.usermanagement.modules.auth.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgetPasswordRequestDto {

    @NotBlank(message = "User Name is Required!")
    private String userName;
}
