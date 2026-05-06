package com.duoc.LearningPlatform.service;

import com.duoc.LearningPlatform.model.PasswordResetCode;
import com.duoc.LearningPlatform.model.User;
import com.duoc.LearningPlatform.repository.PasswordResetCodeRepository;
import com.duoc.LearningPlatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final String SUCCESS_MESSAGE = "If an account exists for this email, password reset instructions will be sent.";
    private static final String INVALID_CODE_MESSAGE = "Invalid or expired verification code.";
    private static final String RESET_SUCCESS_MESSAGE = "Password has been reset successfully.";
    private static final int OTP_LENGTH = 6;

    private final UserRepository userRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetOtpNotifier notifier;
    private final Clock clock;
    private final long otpTtlMinutes;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetCodeRepository passwordResetCodeRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetOtpNotifier notifier,
            Clock clock,
            @Value("${app.password-reset.otp-ttl-minutes:15}") long otpTtlMinutes) {
        this.userRepository = userRepository;
        this.passwordResetCodeRepository = passwordResetCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.notifier = notifier;
        this.clock = clock;
        this.otpTtlMinutes = otpTtlMinutes;
    }

    @Transactional
    public String requestOtp(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            passwordResetCodeRepository.deleteByEmail(email);

            String plainOtp = generateOtp();
            String codeHash = passwordEncoder.encode(plainOtp);
            LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(otpTtlMinutes);

            PasswordResetCode resetCode = new PasswordResetCode(email, codeHash, expiresAt);
            passwordResetCodeRepository.save(resetCode);

            notifier.deliverOtp(email, plainOtp);
        }

        return SUCCESS_MESSAGE;
    }

    @Transactional
    public String resetPassword(String email, String plainOtp, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        PasswordResetCode resetCode = passwordResetCodeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CODE_MESSAGE));

        LocalDateTime now = LocalDateTime.now(clock);
        if (resetCode.isExpired(now) || resetCode.isConsumed()) {
            throw new IllegalArgumentException(INVALID_CODE_MESSAGE);
        }

        if (!passwordEncoder.matches(plainOtp, resetCode.getCodeHash())) {
            throw new IllegalArgumentException(INVALID_CODE_MESSAGE);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CODE_MESSAGE));

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updateEncodedPassword(encodedPassword);
        userRepository.save(user);

        resetCode.markConsumed(now);
        passwordResetCodeRepository.save(resetCode);

        return RESET_SUCCESS_MESSAGE;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
