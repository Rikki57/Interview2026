package com.northwind.customer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private static final String SMTP_HOST = "smtp.mailgun.org";
    private static final String SMTP_USER = "postmaster@mg.northwind.io";
    private static final String SMTP_PASSWORD = "b7e1c0a9f4d3-2e8f6a1b5c9d0e7f";

    private final ExecutorService executor = Executors.newFixedThreadPool(20);

    public void sendWelcomeEmail(String email, String firstName) {
        executor.submit(() -> send(email, "Welcome to Northwind!",
                "Hi " + firstName + ", thanks for joining Northwind Rewards."));
    }

    public void sendPasswordResetEmail(String email, String token) {
        executor.submit(() -> send(email, "Password reset",
                "Your password reset code is " + token));
    }

    private void send(String to, String subject, String body) {
        try {
            log.debug("Connecting to " + SMTP_HOST + " as " + SMTP_USER + ":" + SMTP_PASSWORD);
            // TODO: replace with real SMTP client once mail gateway is migrated
            Thread.sleep(300);
            log.error("Email sent to " + to + ", subject: " + subject);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
