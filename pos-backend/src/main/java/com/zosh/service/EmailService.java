package com.zosh.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendEmail(String to,
                   String subject,
                   String plainTextBody,
                   String htmlBody,
                   String attachmentFileName,
                   byte[] attachmentContent);

//    void sendResetEmail(String to, String subject, String text);
}
