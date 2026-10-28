package com.northwind.customer.util;

import com.northwind.customer.dto.CustomerDto;
import com.northwind.customer.entity.Customer;

public class CustomerMapper {

    public static CustomerDto toDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setEmail(customer.getEmail());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setPhone(customer.getPhone());
        dto.setStatus(customer.getStatus());
        dto.setLoyaltyPoints(customer.getLoyaltyPoints());
        dto.setLoyaltyTier(calculateTier(customer.getLoyaltyPoints()));
        dto.setCreatedAt(DateUtils.format(customer.getCreatedAt()));
        dto.setLastLoginAt(DateUtils.format(customer.getLastLoginAt()));
        return dto;
    }

    private static String calculateTier(Integer points) {
        if (points == null) {
            return "BRONZE";
        }
        if (points >= 10000) {
            return "PLATINUM";
        }
        if (points >= 3000) {
            return "GOLD";
        }
        if (points >= 1000) {
            return "SILVER";
        }
        return "BRONZE";
    }
}
