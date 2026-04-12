package com.zosh.service.impl;

import com.zosh.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name:SmartPos}")
    private String fromName;


    @Override
    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, body, null, null);
    }

    @Override
    public void sendEmail(String to,
                          String subject,
                          String plainTextBody,
                          String htmlBody,
                          String attachmentFileName,
                          byte[] attachmentContent) {
        try {
            log.info("Sending email from '{}' to '{}' with subject '{}'", fromEmail, to, subject);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            helper.setSubject(subject);
            helper.setText(plainTextBody, htmlBody);
            helper.setTo(to);
            helper.setFrom(fromEmail, fromName);

            if (attachmentFileName != null && attachmentContent != null) {
                helper.addAttachment(attachmentFileName, new ByteArrayResource(attachmentContent));
            }

            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully from '{}' to '{}'", fromEmail, to);
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.warn("Email send failed from '{}' to '{}': {}", fromEmail, to, e.getMessage());
            throw new MailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }


}
