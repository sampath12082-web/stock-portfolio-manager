package com.stocks.myportfolio.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.stocks.myportfolio.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String to, String subject, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText("Your OTP code is: " + otpCode + "\n\nThis code expires in 10 minutes.\n\n— MyPortfolio");
            mailSender.send(message);
            log.info("OTP email sent to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send OTP email to {}: {}", to, e.getMessage());
        }
    }
}
