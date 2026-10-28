package com.northwind.customer.controller;

import com.northwind.customer.dto.ImportResult;
import com.northwind.customer.dto.RegisterRequest;
import com.northwind.customer.entity.Customer;
import com.northwind.customer.exception.AuthException;
import com.northwind.customer.repository.CustomerRepository;
import com.northwind.customer.service.CustomerService;
import com.northwind.customer.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String ADMIN_API_KEY = "nw-admin-4f9a2c71e8b3d6";

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SessionService sessionService;

    @GetMapping("/customers")
    public List<Customer> getAllCustomers(@RequestHeader("X-Api-Key") String apiKey) {
        checkApiKey(apiKey);
        return customerRepository.findAll();
    }

    @PostMapping("/customers/import")
    public ImportResult importCustomers(@RequestHeader("X-Api-Key") String apiKey,
                                        @RequestBody List<RegisterRequest> customers) {
        checkApiKey(apiKey);
        return customerService.importCustomers(customers);
    }

    @PostMapping("/customers/{id}/block")
    public ResponseEntity<Void> blockCustomer(@RequestHeader("X-Api-Key") String apiKey, @PathVariable Long id) {
        checkApiKey(apiKey);
        customerService.blockCustomer(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats(@RequestHeader("X-Api-Key") String apiKey) {
        checkApiKey(apiKey);
        List<Customer> customers = customerRepository.findAll();

        Map<String, Long> byStatus = customers.stream()
                .collect(Collectors.groupingBy(Customer::getStatus, Collectors.counting()));
        long totalPoints = customers.stream()
                .mapToLong(Customer::getLoyaltyPoints)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", customers.size());
        stats.put("customersByStatus", byStatus);
        stats.put("totalLoyaltyPoints", totalPoints);
        stats.put("activeSessions", sessionService.activeSessionsCount());
        return stats;
    }

    private void checkApiKey(String apiKey) {
        if (!ADMIN_API_KEY.equals(apiKey)) {
            throw new AuthException("Invalid API key: " + apiKey);
        }
    }
}
