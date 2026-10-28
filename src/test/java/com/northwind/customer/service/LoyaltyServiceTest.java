package com.northwind.customer.service;

import com.northwind.customer.entity.Customer;
import com.northwind.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private LoyaltyService loyaltyService;

    private Customer alice;
    private Customer bob;

    @BeforeEach
    void setUp() {
        alice = Customer.builder().id(1L).loyaltyPoints(500).build();
        bob = Customer.builder().id(2L).loyaltyPoints(100).build();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(bob));
    }

    @Test
    void transferPoints_shouldMovePoints() {
        loyaltyService.transferPoints(1L, 2L, 200);

        assertEquals(300, alice.getLoyaltyPoints());
        assertEquals(300, bob.getLoyaltyPoints());
    }

    @Test
    void transferPoints_shouldBeThreadSafe() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> loyaltyService.transferPoints(1L, 2L, 10));
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue(alice.getLoyaltyPoints() <= 500);
    }
}
