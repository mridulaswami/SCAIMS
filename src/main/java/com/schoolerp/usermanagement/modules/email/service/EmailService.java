package com.schoolerp.usermanagement.modules.email.service;

import com.schoolerp.usermanagement.modules.email.requestDto.EmailRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface EmailService {

    void sendEmail(EmailRequestDto request);
}
