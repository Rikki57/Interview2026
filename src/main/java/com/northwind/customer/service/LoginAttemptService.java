package com.northwind.customer.service;

import com.northwind.customer.model.LoginAttemptKey;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;

    private final Map<LoginAttemptKey, Integer> attempts = new HashMap<>();

    public void loginFailed(LoginAttemptKey key) {
        attempts.merge(key, 1, Integer::sum);
    }

    public void loginSucceeded(LoginAttemptKey key) {
        attempts.remove(key);
    }

    public boolean isBlocked(LoginAttemptKey key) {
        Integer count = attempts.get(key);
        return count != null && count >= MAX_ATTEMPTS;
    }
}
