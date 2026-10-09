package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.EmailVerificationService;

@RestController
public class VerificationController {

    private final EmailVerificationService emailVerificationService;

    public VerificationController(
            EmailVerificationService emailVerificationService) {

        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/verification")
    public ResponseEntity<?> verifyEmail(
            @RequestParam(name = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "Verification token is required."
                    ));
        }

        try {

            long customerId =
                    emailVerificationService.verifyToken(token);

            return ResponseEntity.ok(
                    Map.of(
                            "ok", true,
                            "message", "Email verified successfully.",
                            "customerId", customerId
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(404)
                    .body(Map.of(
                            "ok", false,
                            "message", e.getMessage()
                    ));

        } catch (IllegalStateException e) {

            return ResponseEntity.status(409)
                    .body(Map.of(
                            "ok", false,
                            "message", e.getMessage()
                    ));
        }
    }
}