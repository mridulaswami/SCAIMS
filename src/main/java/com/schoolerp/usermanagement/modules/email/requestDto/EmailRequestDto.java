package com.schoolerp.usermanagement.modules.email.requestDto;

import com.schoolerp.usermanagement.modules.email.constant.EmailSubjectConstant;
import com.schoolerp.usermanagement.modules.email.constant.EmailTemplateConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequestDto {

    @NotBlank
    @Email
    private String to;

    private List<@NotBlank @Email String> toList;

    private List<@Email(message = "Invalid CC email address") String> cc;

    private List<@Email(message = "Invalid BCC email address") String> bcc;

    @NotBlank
    private String subject;

    @NotNull
    private String template;

    private Map<String, Object> variables;
}