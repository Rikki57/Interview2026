package com.northwind.customer.repository;

import com.northwind.customer.entity.Customer;
import com.northwind.customer.security.PasswordHasher;
import com.northwind.customer.service.NotificationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(CustomerRepositoryImpl.class);

    private static final int WELCOME_BONUS = 500;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Customer findCustomerByEmail(String email) {
        try {
            log.info("Looking up customer by email " + email);
            return entityManager
                    .createQuery("select c from Customer c where c.email = :email", Customer.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            log.error("Customer with email " + email + " not found", e);
            throw new RuntimeException("Customer not found: " + email, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Customer> search(String query, String sortBy) {
        try {
            String q = query.toLowerCase();
            String sql = "SELECT * FROM customers WHERE lower(first_name) LIKE '%" + q + "%'"
                    + " OR lower(last_name) LIKE '%" + q + "%'"
                    + " OR lower(email) LIKE '%" + q + "%'"
                    + " ORDER BY " + sortBy;
            log.debug("Executing search: " + sql);
            return entityManager.createNativeQuery(sql, Customer.class).getResultList();
        } catch (Exception e) {
            log.error("Search failed", e);
            throw new RuntimeException("Search failed", e);
        }
    }

    @Override
    @Transactional
    public Customer registerCustomer(Customer customer, String rawPassword) {
        try {
            if (rawPassword == null || rawPassword.length() < 8) {
                throw new IllegalArgumentException("Password must be at least 8 characters long");
            }
            customer.setEmail(customer.getEmail().trim());
            customer.setPasswordHash(passwordHasher.hash(rawPassword));
            customer.setStatus("ACTIVE");
            customer.setRole("USER");
            customer.setLoyaltyPoints(WELCOME_BONUS);
            customer.setCreatedAt(new Date());
            customer.setUpdatedAt(new Date());

            entityManager.persist(customer);
            notificationService.sendWelcomeEmail(customer.getEmail(), customer.getFirstName());

            log.info("Customer saved: " + customer);
            return customer;
        } catch (Exception e) {
            log.error("Failed to register customer", e);
            throw new RuntimeException("Failed to register customer: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int deactivateInactiveCustomers(int days) {
        List<Customer> customers = entityManager
                .createQuery("select c from Customer c", Customer.class)
                .getResultList();

        Date threshold = new Date(System.currentTimeMillis() - days * 24 * 60 * 60 * 1000);
        int count = 0;
        for (Customer customer : customers) {
            if (customer.getLastLoginAt() == null || customer.getLastLoginAt().before(threshold)) {
                if (!"ADMIN".equals(customer.getRole())) {
                    customer.setStatus("INACTIVE");
                    customer.setUpdatedAt(new Date());
                    count++;
                }
            }
        }
        log.info("Deactivated " + count + " customers");
        return count;
    }
}
