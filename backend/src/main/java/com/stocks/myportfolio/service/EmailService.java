package com.stocks.myportfolio.service;

public interface EmailService {

    void sendOtpEmail(String to, String subject, String otpCode);
}
