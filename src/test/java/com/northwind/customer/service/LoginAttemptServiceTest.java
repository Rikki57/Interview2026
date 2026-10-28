package com.northwind.customer.service;

import com.northwind.customer.model.LoginAttemptKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAttemptServiceTest {

    private final LoginAttemptService service = new LoginAttemptService();

    @Test
    void shouldBlockAfterMaxAttempts() {
        LoginAttemptKey key = new LoginAttemptKey("john.doe@example.com", "10.0.0.1");

        for (int i = 0; i < 5; i++) {
            service.loginFailed(key);
        }

        assertTrue(service.isBlocked(key));
    }

    @Test
    void shouldResetAttemptsAfterSuccessfulLogin() {
        LoginAttemptKey key = new LoginAttemptKey("john.doe@example.com", "10.0.0.1");

        for (int i = 0; i < 5; i++) {
            service.loginFailed(key);
        }
        service.loginSucceeded(key);

        assertFalse(service.isBlocked(key));
    }
}
