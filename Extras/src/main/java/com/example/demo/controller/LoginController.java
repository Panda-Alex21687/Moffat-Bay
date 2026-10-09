package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.repository.CustomerRepository;
import com.example.demo.model.Customer;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class LoginController {
	
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder(12);

    public LoginController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(
    @RequestBody Map<String, String> request,
    HttpSession session) {
            

        String email = request.get("email");
        String password = request.get("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            return ResponseEntity
                    .unprocessableContent()
                    .body(Map.of(
                            "ok", false,
                            "message", "Email and password are required."
                    ));
        }

        Customer customer = customerRepository.findByEmail(email);

        if (customer == null) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "ok", false,
                            "message", "Invalid email or password."
                    ));
        }
        if (!customer.isEmailVerified()) {
            return ResponseEntity
                .status(403)
                .body(Map.of(
                    "ok", false,
                    "message", "Please verify your email before logging in."
                ));
        }
        if (!passwordEncoder.matches(password, customer.getPasswordHash())) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "ok", false,
                            "message", "Invalid email or password."
                    ));
        }
        
        session.setAttribute("customer_id", customer.getCustomerId());
        session.setAttribute("firstName", customer.getFirstName());
        session.setAttribute("email", customer.getEmail());
        
        return ResponseEntity.ok(
                Map.of(
                        "ok", true,
                        "message", "Login successful.",
                        "customerId", customer.getCustomerId(),
                        "email", customer.getEmail(),
                        "firstName", customer.getFirstName(),
                        "redirect", "post_login.html"
                )
        );
    }

}