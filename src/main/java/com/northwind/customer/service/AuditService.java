package com.northwind.customer.service;

import com.northwind.customer.model.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private static final int SUSPICIOUS_LOGINS_THRESHOLD = 20;

    private final List<AuditEvent> events = new ArrayList<>();

    @Autowired
    @Lazy
    private SessionService sessionService;

    public synchronized void record(Long customerId, String action) {
        events.add(new AuditEvent(customerId, action, new Date()));
        log.info("Audit event " + action + " for customer " + customerId);
    }

    @Scheduled(fixedRate = 30_000)
    public synchronized void flush() {
        if (events.isEmpty()) {
            return;
        }

        Map<Long, Long> loginsPerCustomer = events.stream()
                .filter(e -> "LOGIN".equals(e.getAction()))
                .collect(Collectors.groupingBy(AuditEvent::getCustomerId, Collectors.counting()));

        loginsPerCustomer.forEach((customerId, count) -> {
            if (count > SUSPICIOUS_LOGINS_THRESHOLD) {
                log.info("Suspicious activity for customer " + customerId + ", dropping all sessions");
                sessionService.invalidateAllForCustomer(customerId);
            }
        });

        log.debug("Flushed " + events.size() + " audit events");
        events.clear();
    }
}
