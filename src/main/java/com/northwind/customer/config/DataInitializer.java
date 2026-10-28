package com.northwind.customer.config;

import com.northwind.customer.entity.Customer;
import com.northwind.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        if (customerRepository.findByEmail("admin@northwind.io").isPresent()) {
            return;
        }

        Customer admin = new Customer();
        admin.setEmail("admin@northwind.io");
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin = customerRepository.registerCustomer(admin, "Northwind#2023");
        admin.setRole("ADMIN");
        customerRepository.save(admin);

        log.info("Default admin account created: admin@northwind.io / Northwind#2023");
    }
}
