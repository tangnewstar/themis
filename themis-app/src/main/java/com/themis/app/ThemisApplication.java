package com.themis.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main Spring Boot application for Themis Rule Engine
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.themis.app",
        "com.themis.api",
        "com.themis.ai",
        "com.themis.engine",
        "com.themis.metadata",
        "com.themis.orchestration"
})
public class ThemisApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ThemisApplication.class, args);
    }
}