package com.sih.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder; // Add this import
import org.springframework.context.annotation.Bean;             // Add this import
import org.springframework.web.client.RestTemplate;             // Add this import
import java.time.Duration;                                    // Add this import

@SpringBootApplication
public class ErpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpBackendApplication.class, args);
    }

    // --- ADD THIS NEW BEAN ---
    // This creates a global RestTemplate that all services will use.
    // It's configured to time out if a connection isn't made or a response isn't received.
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }
}