package com.northwind.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointsTransferRequest {

    private Long toCustomerId;

    private int points;
}
