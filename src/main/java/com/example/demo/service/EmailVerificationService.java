package com.example.demo.service;

import com.example.demo.model.EmailVerification;
import com.example.demo.repository.EmailVerificationRepository;
import com.example.demo.repository.CustomerRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final CustomerRepository customerRepository;

    public EmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
            CustomerRepository customerRepository) {

        this.emailVerificationRepository = emailVerificationRepository;
        this.customerRepository = customerRepository;
    }

    public String createVerificationToken(long customerId) {

        String rawToken = generateToken();

        EmailVerification verification = new EmailVerification();

        verification.setCustomerId(customerId);
        verification.setTokenHash(hashToken(rawToken));
        verification.setExpiresAt(
                LocalDateTime.now().plusHours(24)
        );

        emailVerificationRepository.save(verification);

        return rawToken;
    }
    
    @Transactional
    public long verifyToken(String rawToken) {
    	
    	
    if (rawToken == null || rawToken.isBlank()) {
        throw new IllegalArgumentException("Verification token is missing.");
    }

    String hashedToken = hashToken(rawToken);

    EmailVerification verification =
            emailVerificationRepository.findValid(rawToken, hashedToken);

    if (verification == null) {
        throw new IllegalArgumentException(
                "The verification link is invalid or has expired."
        );
    }

    int verificationUpdated =
            emailVerificationRepository.markVerified(
                    verification.getVerificationId()
            );

    if (verificationUpdated == 0) {
        throw new IllegalStateException(
                "The verification token could not be marked as verified."
        );
    }

    int customerUpdated =
            customerRepository.markEmailVerified(
                    verification.getCustomerId()
            );

    if (customerUpdated == 0) {
        throw new IllegalStateException(
                "The customer account could not be marked as verified."
        );
    }

    return verification.getCustomerId();
}

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash verification token", e);
        }
    }
}