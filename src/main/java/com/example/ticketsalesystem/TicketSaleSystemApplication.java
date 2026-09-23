package com.example.ticketsalesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TicketSaleSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketSaleSystemApplication.class, args);
    }

}
