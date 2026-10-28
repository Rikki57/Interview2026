package com.northwind.customer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class SessionInfo {

    private Long customerId;

    private Date createdAt;
}
