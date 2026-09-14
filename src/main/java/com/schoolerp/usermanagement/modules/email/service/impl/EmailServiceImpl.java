package com.schoolerp.usermanagement.modules.email.service.impl;

import com.schoolerp.usermanagement.modules.email.requestDto.EmailRequestDto;
import com.schoolerp.usermanagement.modules.email.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    @Async("emailTaskExecutor")
    public void sendEmail(EmailRequestDto request) {

        try {

            /*
             * Prepare Thymeleaf context
             */
            Context context = new Context();

            if (request.getVariables() != null) {
                context.setVariables(request.getVariables());
            }

            /*
             * Process HTML template
             */
            String htmlContent = templateEngine.process(request.getTemplate(), context);

            /*
             * Create MIME message
             */
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            /*
             * FROM
             */
            helper.setFrom(from, "SCAIMS");

            /*
             * TO
             */
            if (request.getTo() != null && !request.getTo().isBlank()) {

                helper.setTo(request.getTo());

            } else if (request.getToList() != null && !request.getToList().isEmpty()) {

                helper.setTo(request.getToList().toArray(new String[0]));
            }

            /*
             * CC
             */
            if (request.getCc() != null && !request.getCc().isEmpty()) {
                helper.setCc(request.getCc().toArray(new String[0]));
            }

            /*
             * BCC
             */
            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                helper.setBcc(request.getBcc().toArray(new String[0]));
            }
            /*
             * Subject
             */
            helper.setSubject(request.getSubject());

            /*
             * HTML Body
             */
            helper.setText(htmlContent, true);

            /*
             * SEND THROUGH SMTP
             */
            javaMailSender.send(message);

            log.info("Email sent successfully to {}", request.getTo());

        } catch (Exception exception) {

            log.error("Failed to send email to {}", request.getTo(), exception);
        }
    }
}