package com.northwind.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private String status;

    private Integer loyaltyPoints;

    private String loyaltyTier;

    private String createdAt;

    private String lastLoginAt;
}
