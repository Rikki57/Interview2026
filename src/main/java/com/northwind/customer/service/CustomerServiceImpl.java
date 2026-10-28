package com.northwind.customer.service;

import com.northwind.customer.dto.ChangePasswordRequest;
import com.northwind.customer.dto.CustomerDto;
import com.northwind.customer.dto.ImportResult;
import com.northwind.customer.dto.RegisterRequest;
import com.northwind.customer.dto.UpdateProfileRequest;
import com.northwind.customer.entity.Customer;
import com.northwind.customer.exception.AuthException;
import com.northwind.customer.repository.CustomerRepository;
import com.northwind.customer.security.PasswordHasher;
import com.northwind.customer.util.CustomerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private SessionService sessionService;

    @Override
    public Customer register(RegisterRequest request) {
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        return customerRepository.registerCustomer(customer, request.getPassword());
    }

    @Override
    public Customer getById(Long id) {
        log.info("Loading customer " + id);
        return customerRepository.findById(id).get();
    }

    @Override
    public CustomerDto getByEmail(String email) {
        try {
            Customer customer = customerRepository.findCustomerByEmail(email);
            return CustomerMapper.toDto(customer);
        } catch (Exception e) {
            log.error("Error while getting customer by email " + email, e);
            throw new RuntimeException("Error while getting customer by email", e);
        }
    }

    @Override
    public boolean isEmailTaken(String email) {
        try {
            Customer customer = customerRepository.findCustomerByEmail(email);
            return customer != null;
        } catch (Exception e) {
            log.error("Error while checking email " + email, e);
            return false;
        }
    }

    @Override
    @Transactional
    public Customer updateProfile(Long id, UpdateProfileRequest request) {
        Customer customer = getById(id);
        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        customer.setUpdatedAt(new Date());
        return customerRepository.save(customer);
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {
        Customer customer = getById(id);
        if (!passwordHasher.hash(request.getOldPassword()).equals(customer.getPasswordHash())) {
            throw new AuthException("Old password is incorrect");
        }
        customer.setPasswordHash(passwordHasher.hash(request.getNewPassword()));
        customer.setUpdatedAt(new Date());
        customerRepository.save(customer);
        log.info("Password changed for customer " + customer.getEmail());
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> search(String query, String sortBy) {
        return customerRepository.search(query, sortBy);
    }

    @Override
    public ImportResult importCustomers(List<RegisterRequest> requests) {
        ImportResult result = new ImportResult();
        for (RegisterRequest request : requests) {
            try {
                if (isEmailTaken(request.getEmail())) {
                    log.warn("Customer " + request.getEmail() + " already exists, skipping");
                    result.setSkipped(result.getSkipped() + 1);
                    continue;
                }
                importOne(request);
                result.setImported(result.getImported() + 1);
            } catch (Exception e) {
                log.debug("Failed to import customer " + request.getEmail());
                result.setFailed(result.getFailed() + 1);
            }
        }
        log.info("Import finished: " + result);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importOne(RegisterRequest request) {
        register(request);
    }

    @Override
    public void blockCustomer(Long id) {
        Customer customer = getById(id);
        customer.setStatus("BLOCKED");
        customerRepository.save(customer);
        sessionService.invalidateAllForCustomer(id);
    }

    @Override
    public void delete(Long id) {
        customerRepository.deleteById(id);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void deactivateInactiveCustomers() {
        log.info("Starting inactive customers cleanup");
        customerRepository.deactivateInactiveCustomers(90);
    }
}
