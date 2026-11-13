package com.examly.springapp.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cross origin configuration of the catalogue.
 *
 * <p>The client application is served from a development server on port 8081. Both loopback
 * spellings of that host are accepted so that the API can be reached through
 * {@code http://localhost:8081} and {@code http://127.0.0.1:8081}. The registration covers the
 * form oriented endpoints as well as the versioned catalogue endpoints.</p>
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    /** Origins allowed to call the API. */
    private static final String[] ALLOWED_ORIGINS = {"http://localhost:8081", "http://127.0.0.1:8081"};

    /** HTTP methods allowed on the API. */
    private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE", "OPTIONS"};

    /** Headers a client is allowed to send. */
    private static final String ALLOWED_HEADERS = "*";

    /** Headers a client is allowed to read from the response. */
    private static final String[] EXPOSED_HEADERS = {"Location", "Content-Disposition"};

    /** Time in seconds a browser may cache the pre flight response. */
    private static final long MAX_AGE_SECONDS = 3600L;

    /**
     * Registers the cross origin rules for every endpoint of the application.
     *
     * @param registry registry of the cross origin mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(ALLOWED_ORIGINS)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALLOWED_HEADERS)
                .exposedHeaders(EXPOSED_HEADERS)
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECONDS);

        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGINS)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALLOWED_HEADERS)
                .exposedHeaders(EXPOSED_HEADERS)
                .allowCredentials(true)
                .maxAge(MAX_AGE_SECONDS);
    }
}
