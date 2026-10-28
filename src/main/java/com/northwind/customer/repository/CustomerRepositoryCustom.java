package com.northwind.customer.repository;

import com.northwind.customer.entity.Customer;

import java.util.List;

public interface CustomerRepositoryCustom {

    Customer findCustomerByEmail(String email);

    List<Customer> search(String query, String sortBy);

    Customer registerCustomer(Customer customer, String rawPassword);

    int deactivateInactiveCustomers(int days);
}
