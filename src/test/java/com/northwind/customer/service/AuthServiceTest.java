package com.northwind.customer.service;

import com.northwind.customer.dto.LoginRequest;
import com.northwind.customer.dto.LoginResponse;
import com.northwind.customer.entity.Customer;
import com.northwind.customer.exception.AuthException;
import com.northwind.customer.repository.CustomerRepository;
import com.northwind.customer.security.JwtTokenProvider;
import com.northwind.customer.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SessionService sessionService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(42L);
        customer.setEmail("john.doe@example.com");
        customer.setPasswordHash("hash");
        customer.setStatus("ACTIVE");
    }

    @Test
    void login_success() {
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(customer));
        when(passwordHasher.hash("secret123")).thenReturn("hash");
        when(jwtTokenProvider.generateToken(customer)).thenReturn("token");

        LoginResponse response = authService.login(new LoginRequest("john.doe@example.com", "secret123"), "127.0.0.1");

        assertEquals("token", response.getToken());
    }

    @Test
    void login_shouldFailWithWrongPassword() {
        when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(customer));
        when(passwordHasher.hash("wrong")).thenReturn("other-hash");

        assertThrows(Exception.class,
                () -> authService.login(new LoginRequest("john.doe@example.com", "wrong"), "127.0.0.1"));
    }

    @Test
    void login_shouldBlockAfterFiveFailedAttempts() {
        when(loginAttemptService.isBlocked(any())).thenReturn(true);

        assertThrows(AuthException.class,
                () -> authService.login(new LoginRequest("john.doe@example.com", "secret123"), "127.0.0.1"));
    }
}
