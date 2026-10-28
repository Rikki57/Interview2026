package com.northwind.customer.security;

import com.northwind.customer.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider();

    @Test
    void shouldGenerateValidToken() {
        Customer customer = Customer.builder().id(7L).email("john.doe@example.com").role("USER").build();

        String token = provider.generateToken(customer);

        assertTrue(provider.validateToken(token));
        assertEquals(7L, provider.getCustomerId(token));
    }

    @Test
    void shouldGenerateUniqueTokens() throws InterruptedException {
        Customer customer = Customer.builder().id(7L).email("john.doe@example.com").role("USER").build();

        String first = provider.generateToken(customer);
        Thread.sleep(1500);
        String second = provider.generateToken(customer);

        assertNotEquals(first, second);
    }

    @Test
    void shouldRejectGarbageToken() {
        assertFalse(provider.validateToken("not-a-token"));
    }
}
