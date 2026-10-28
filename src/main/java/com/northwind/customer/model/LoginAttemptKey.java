package com.northwind.customer.model;

import java.util.Objects;

/**
 * Identifies a series of login attempts from the same client.
 */
public class LoginAttemptKey {

    private final String email;
    private final String ip;
    private final long createdAt;

    public LoginAttemptKey(String email, String ip) {
        this.email = email;
        this.ip = ip;
        this.createdAt = System.currentTimeMillis();
    }

    public String getEmail() {
        return email;
    }

    public String getIp() {
        return ip;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean equals(LoginAttemptKey other) {
        if (other == null) {
            return false;
        }
        return email.equalsIgnoreCase(other.email) && ip.equals(other.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, ip, createdAt);
    }

    @Override
    public String toString() {
        return "LoginAttemptKey{email='" + email + "', ip='" + ip + "'}";
    }
}
