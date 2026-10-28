package com.northwind.customer.service;

import com.northwind.customer.model.SessionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SessionService {

    private final Map<String, SessionInfo> sessions = new HashMap<>();

    @Autowired
    private AuditService auditService;

    public synchronized void registerSession(String token, Long customerId) {
        sessions.put(token, new SessionInfo(customerId, new Date()));
        auditService.record(customerId, "LOGIN");
    }

    public synchronized void invalidate(String token) {
        SessionInfo session = sessions.remove(token);
        if (session != null) {
            auditService.record(session.getCustomerId(), "LOGOUT");
        }
    }

    public synchronized boolean isActive(String token) {
        return sessions.containsKey(token);
    }

    public synchronized void invalidateAllForCustomer(Long customerId) {
        sessions.entrySet().removeIf(entry -> {
            if (entry.getValue().getCustomerId().equals(customerId)) {
                auditService.record(customerId, "FORCED_LOGOUT");
                return true;
            }
            return false;
        });
    }

    public int activeSessionsCount() {
        return sessions.size();
    }
}
