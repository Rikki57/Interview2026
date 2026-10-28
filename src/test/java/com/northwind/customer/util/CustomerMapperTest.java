package com.northwind.customer.util;

import com.northwind.customer.dto.CustomerDto;
import com.northwind.customer.entity.Customer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerMapperTest {

    @BeforeAll
    static void setUpTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void toDto_shouldMapAllFields() {
        Customer customer = Customer.builder()
                .id(1L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .status("ACTIVE")
                .loyaltyPoints(5000)
                .createdAt(new Date(0))
                .build();

        CustomerDto dto = CustomerMapper.toDto(customer);

        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("GOLD", dto.getLoyaltyTier());
        assertEquals("1970-01-01 00:00:00", dto.getCreatedAt());
    }
}
