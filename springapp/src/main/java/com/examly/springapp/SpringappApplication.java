package com.examly.springapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Vegan Snacks application.
 *
 * <p>The application exposes the catalogue of vegan snacks as a REST API and stores its data in
 * an embedded in memory database, so it starts without any external dependency.</p>
 */
@SpringBootApplication
public class SpringappApplication {

    /**
     * Starts the application.
     *
     * @param args command line arguments supplied to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(SpringappApplication.class, args);
    }
}
