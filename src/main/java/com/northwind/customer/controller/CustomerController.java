package com.northwind.customer.controller;

import com.northwind.customer.dto.ChangePasswordRequest;
import com.northwind.customer.dto.CustomerDto;
import com.northwind.customer.dto.PointsTransferRequest;
import com.northwind.customer.dto.UpdateProfileRequest;
import com.northwind.customer.entity.Customer;
import com.northwind.customer.repository.CustomerRepository;
import com.northwind.customer.service.CustomerService;
import com.northwind.customer.service.LoyaltyService;
import com.northwind.customer.util.CustomerMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoyaltyService loyaltyService;

    @GetMapping("/me")
    public CustomerDto getCurrentCustomer(HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("customerId");
        Customer customer = customerService.getById(customerId);
        CustomerDto dto = CustomerMapper.toDto(customer);

        int points = customer.getLoyaltyPoints();
        if (points > 10000) {
            dto.setLoyaltyTier("PLATINUM");
        } else if (points > 5000) {
            dto.setLoyaltyTier("GOLD");
        } else if (points > 1000) {
            dto.setLoyaltyTier("SILVER");
        } else {
            dto.setLoyaltyTier("BRONZE");
        }
        return dto;
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest body, HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("customerId");

        if (body.getPhone() != null && !body.getPhone().matches("^\\+?[0-9]{10,15}$")) {
            return ResponseEntity.badRequest().body("Invalid phone number");
        }
        if (body.getEmail() != null) {
            Optional<Customer> existing = customerRepository.findByEmail(body.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(customerId)) {
                return ResponseEntity.badRequest().body("Email is already in use");
            }
        }

        Customer updated = customerService.updateProfile(customerId, body);
        return ResponseEntity.ok(CustomerMapper.toDto(updated));
    }

    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest body, HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("customerId");
        customerService.changePassword(customerId, body);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/me/points/transfer")
    public ResponseEntity<Void> transferPoints(@RequestBody PointsTransferRequest body, HttpServletRequest request) {
        Long customerId = (Long) request.getAttribute("customerId");
        loyaltyService.transferPoints(customerId, body.getToCustomerId(), body.getPoints());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<CustomerDto> getAll() {
        return customerService.findAll().stream()
                .map(CustomerMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Customer getById(@PathVariable Long id) {
        return customerService.getById(id);
    }

    @GetMapping("/search")
    public List<Customer> search(@RequestParam("q") String query,
                                 @RequestParam(value = "sort", defaultValue = "id") String sort) {
        return customerService.search(query, sort);
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookupByEmail(@RequestParam String email) {
        try {
            log.info("Lookup customer by email " + email);
            CustomerDto dto = customerService.getByEmail(email);
            if (dto == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("Failed to lookup customer " + email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok().build();
    }
}
