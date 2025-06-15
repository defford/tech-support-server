package com.techsupport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Tech Support Server application.
 * 
 * This Spring Boot application provides a REST API for managing
 * tech support tickets, appointments, feedback, and reporting.
 */
@SpringBootApplication
public class TechSupportApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechSupportApplication.class, args);
    }
} 