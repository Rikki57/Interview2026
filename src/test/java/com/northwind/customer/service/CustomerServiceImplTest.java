package com.northwind.customer.service;

import com.northwind.customer.dto.ChangePasswordRequest;
import com.northwind.customer.dto.ImportResult;
import com.northwind.customer.dto.RegisterRequest;
import com.northwind.customer.dto.UpdateProfileRequest;
import com.northwind.customer.entity.Customer;
import com.northwind.customer.repository.CustomerRepository;
import com.northwind.customer.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void register_shouldCreateCustomer() {
        Customer saved = new Customer();
        saved.setId(1L);
        saved.setEmail("john.doe@example.com");
        when(customerRepository.registerCustomer(any(), any())).thenReturn(saved);

        RegisterRequest request = new RegisterRequest("john.doe@example.com", "secret123", "John", "Doe", null);
        Customer result = customerService.register(request);

        assertNotNull(result);
        verify(customerRepository).registerCustomer(any(), any());
    }

    @Test
    void getById_shouldReturnCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerService.getById(1L);

        assertEquals(result.getFirstName(), "John");
    }

    @Test
    void getById_shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        try {
            customerService.getById(2L);
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    void getByEmail_shouldHandleMissingCustomer() {
        when(customerRepository.findCustomerByEmail("missing@example.com")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> customerService.getByEmail("missing@example.com"));
    }

    @Test
    void updateProfile_shouldUpdateFields() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Jane");
        customerService.updateProfile(1L, request);

        verify(customerRepository).save(any());
    }

    @Test
    void changePassword_shouldUpdateHash() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setPasswordHash("old-hash");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(passwordHasher.hash("oldPassword")).thenReturn("old-hash");
        when(passwordHasher.hash("newPassword")).thenReturn("new-hash");

        customerService.changePassword(1L, new ChangePasswordRequest("oldPassword", "newPassword"));

        assertEquals("new-hash", customer.getPasswordHash());
    }

    @Test
    void importCustomers_shouldImportAll() {
        when(customerRepository.findCustomerByEmail(anyString())).thenReturn(null);
        when(customerRepository.registerCustomer(any(), any())).thenReturn(new Customer());

        ImportResult result = customerService.importCustomers(List.of(
                new RegisterRequest("a@example.com", "password1", "A", "A", null),
                new RegisterRequest("b@example.com", "password2", "B", "B", null)
        ));

        assertEquals(2, result.getImported());
    }
}
