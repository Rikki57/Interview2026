package com.northwind.customer.service;

import com.northwind.customer.dto.ChangePasswordRequest;
import com.northwind.customer.dto.CustomerDto;
import com.northwind.customer.dto.ImportResult;
import com.northwind.customer.dto.RegisterRequest;
import com.northwind.customer.dto.UpdateProfileRequest;
import com.northwind.customer.entity.Customer;

import java.util.List;

public interface CustomerService {

    Customer register(RegisterRequest request);

    Customer getById(Long id);

    CustomerDto getByEmail(String email);

    boolean isEmailTaken(String email);

    Customer updateProfile(Long id, UpdateProfileRequest request);

    void changePassword(Long id, ChangePasswordRequest request);

    List<Customer> findAll();

    List<Customer> search(String query, String sortBy);

    ImportResult importCustomers(List<RegisterRequest> requests);

    void blockCustomer(Long id);

    void delete(Long id);
}
