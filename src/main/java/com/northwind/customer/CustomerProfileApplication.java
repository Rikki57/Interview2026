package com.northwind.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CustomerProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerProfileApplication.class, args);
    }
}
