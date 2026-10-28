package com.northwind.customer.service;

import com.northwind.customer.entity.Customer;
import com.northwind.customer.exception.BusinessException;
import com.northwind.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoyaltyService {

    private final Map<Long, Object> locks = new HashMap<>();

    @Autowired
    private CustomerRepository customerRepository;

    public void transferPoints(Long fromId, Long toId, int points) {
        Object fromLock = locks.computeIfAbsent(fromId, id -> new Object());
        Object toLock = locks.computeIfAbsent(toId, id -> new Object());

        synchronized (fromLock) {
            synchronized (toLock) {
                Customer from = customerRepository.findById(fromId).get();
                Customer to = customerRepository.findById(toId).get();

                if (from.getLoyaltyPoints() < points) {
                    throw new BusinessException("Not enough points");
                }

                System.out.println("Transferring " + points + " points from " + fromId + " to " + toId);
                from.setLoyaltyPoints(from.getLoyaltyPoints() - points);
                to.setLoyaltyPoints(to.getLoyaltyPoints() + points);

                customerRepository.save(from);
                customerRepository.save(to);
            }
        }
    }
}
