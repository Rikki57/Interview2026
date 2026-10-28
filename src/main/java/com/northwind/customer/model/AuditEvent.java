package com.northwind.customer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class AuditEvent {

    private Long customerId;

    private String action;

    private Date timestamp;
}
