package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Boat;
import com.example.demo.model.Customer;
import com.example.demo.repository.BoatRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.EmailVerificationService;

import jakarta.mail.MessagingException;

import com.example.demo.service.EmailService;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    private final CustomerRepository customerRepository;
    private final BoatRepository boatRepository;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    
    @Value("${moffat.base-url:http://localhost:8080}")
    private String baseUrl;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder(12);

    public RegistrationController(
            CustomerRepository customerRepository,
            BoatRepository boatRepository,
            EmailVerificationService emailVerificationService,
            EmailService emailService) {

        this.customerRepository = customerRepository;
        this.boatRepository = boatRepository;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) throws MessagingException {

        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String phone = request.get("phone");
        String street = request.get("street");
        String city = request.get("city");
        String state = request.get("state");
        String zip = request.get("zip");
        String email = request.get("email");
        String password = request.get("password");

        String boatName = request.get("boatName");
        String boatType = request.get("boatType");
        String registrationNumber = request.get("registrationNumber");
        String boatLengthText = request.get("boatLength");

        if (isBlank(firstName)
                || isBlank(lastName)
                || isBlank(email)
                || isBlank(password)
                || isBlank(boatName)
                || isBlank(boatLengthText)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "Required fields are missing."
                    ));
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "Please enter a valid email address."
                    ));
        }

        if (zip == null || !zip.matches("^\\d{5}(-\\d{4})?$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "Please enter a valid ZIP code."
                    ));
        }

        if (state == null || !state.matches("^[A-Za-z]{2}$")) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "State must be a two-letter abbreviation."
                    ));
        }
        
        if (customerRepository.emailExists(email)) {
            return ResponseEntity.status(409)
                    .body(Map.of(
                            "ok", false,
                            "message", "An account with this email already exists."
                    ));
        }

        if (!isValidPassword(password)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message",
                            "Password must be at least 8 characters and contain "
                            + "uppercase, lowercase, digit, and special character."
                    ));
        }

        BigDecimal boatLength;

        try {
            boatLength = new BigDecimal(boatLengthText);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "Boat length must be a valid number."
                    ));
        }

        if (boatLength.compareTo(BigDecimal.ONE) < 0
                || boatLength.compareTo(new BigDecimal("200")) > 0) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "ok", false,
                            "message", "Boat length must be between 1 and 200 feet."
                    ));
        }

        Customer customer = new Customer();

        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPhone(phone);
        customer.setStreet(street);
        customer.setCity(city);
        customer.setState(state);
        customer.setZip(zip);
        customer.setEmail(email);
        customer.setPasswordHash(passwordEncoder.encode(password));
        customer.setEmailVerified(false);

        long customerId = customerRepository.save(customer);

        Boat boat = new Boat();

        boat.setCustomerId(customerId);
        boat.setBoatName(boatName);
        boat.setBoatLengthFt(boatLength);
        boat.setBoatType(boatType);
        boat.setRegistrationNumber(registrationNumber);

        long boatId = boatRepository.save(boat);

        String verificationToken =
                emailVerificationService.createVerificationToken(customerId);
       
                emailService.sendVerificationEmail(
                email,
                firstName,
                verificationToken
        );

                return ResponseEntity.ok(
                        Map.of(
                                "ok", true,
                                "message", "Registration successful. Please verify your email.",
                                "customerId", customerId,
                                "boatId", boatId,
                                "emailVerified", false,
                                "verificationUrl",
                                emailService.buildVerificationUrl(verificationToken)
                        )
                );
    }

    private boolean isValidPassword(String password) {

        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[^A-Za-z0-9].*");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}