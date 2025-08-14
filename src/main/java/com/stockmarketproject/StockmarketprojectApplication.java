package com.stockmarketproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockmarketprojectApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockmarketprojectApplication.class, args);
    }
}