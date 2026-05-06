package com.duoc.LearningPlatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsolePasswordResetOtpNotifier implements PasswordResetOtpNotifier {

    private static final Logger logger = LoggerFactory.getLogger(ConsolePasswordResetOtpNotifier.class);

    @Override
    public void deliverOtp(String email, String plainOtp) {
        logger.info("Password reset OTP for {}: {}", email, plainOtp);
    }
}
