package com.northwind.customer.service;

import com.northwind.customer.dto.LoginRequest;
import com.northwind.customer.dto.LoginResponse;
import com.northwind.customer.entity.Customer;
import com.northwind.customer.exception.AuthException;
import com.northwind.customer.model.LoginAttemptKey;
import com.northwind.customer.repository.CustomerRepository;
import com.northwind.customer.security.JwtTokenProvider;
import com.northwind.customer.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    public LoginResponse login(LoginRequest request, String ip) {
        LoginAttemptKey key = new LoginAttemptKey(request.getEmail(), ip);
        if (loginAttemptService.isBlocked(key)) {
            throw new AuthException("Too many failed attempts, try again later");
        }

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("User with email " + request.getEmail() + " not found"));

        if (!passwordHasher.hash(request.getPassword()).equals(customer.getPasswordHash())) {
            loginAttemptService.loginFailed(key);
            log.debug("Wrong password for " + request.getEmail() + ": " + request.getPassword());
            throw new AuthException("Wrong password");
        }

        if (customer.getStatus().equals("BLOCKED")) {
            throw new AuthException("Account is blocked");
        }

        loginAttemptService.loginSucceeded(key);

        String token = jwtTokenProvider.generateToken(customer);
        sessionService.registerSession(token, customer.getId());

        customer.setLastLoginAt(new Date());
        customerRepository.save(customer);

        log.info("Customer logged in: " + customer + ", token=" + token);
        return new LoginResponse(token, customer.getId(), JwtTokenProvider.EXPIRATION_MS / 1000);
    }

    public void logout(String token) {
        sessionService.invalidate(token);
    }
}
