package com.northwind.customer.controller;

import com.northwind.customer.dto.LoginRequest;
import com.northwind.customer.dto.LoginResponse;
import com.northwind.customer.dto.RegisterRequest;
import com.northwind.customer.entity.Customer;
import com.northwind.customer.repository.CustomerRepository;
import com.northwind.customer.security.PasswordHasher;
import com.northwind.customer.service.AuthService;
import com.northwind.customer.service.CustomerService;
import com.northwind.customer.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Password is too short");
        }
        if (customerService.isEmailTaken(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already registered");
        }

        Customer customer = customerService.register(request);
        log.info("New customer registered: " + customer);
        return ResponseEntity.ok(customer);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        log.info("Login attempt: " + request);
        return authService.login(request, httpRequest.getRemoteAddr());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        authService.logout(authorization.substring(7));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "No account registered for " + email));
        }

        String token = String.valueOf(new Random().nextInt(900000) + 100000);
        Customer c = customer.get();
        c.setPasswordResetToken(token);
        customerRepository.save(c);

        notificationService.sendPasswordResetEmail(c.getEmail(), token);
        log.info("Password reset token for " + email + " is " + token);
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String token,
                                           @RequestParam String newPassword) {
        Customer customer = customerRepository.findByEmail(email).get();
        if (token.equals(customer.getPasswordResetToken())) {
            customer.setPasswordHash(passwordHasher.hash(newPassword));
            customerRepository.save(customer);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Invalid reset token");
    }
}
