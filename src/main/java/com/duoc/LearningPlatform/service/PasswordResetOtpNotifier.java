package com.duoc.LearningPlatform.service;

public interface PasswordResetOtpNotifier {

    void deliverOtp(String email, String plainOtp);
}
