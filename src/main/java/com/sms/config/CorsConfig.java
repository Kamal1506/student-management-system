package com.sms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    // ─── CORS FILTER ──────────────────────────────────────────────────────────
    // Without this, the browser will BLOCK your frontend from calling
    // the backend API (even on localhost) — this is a browser security rule.
    //
    // This config allows:
    //   • Requests from your frontend (localhost:8080 for local dev)
    //   • All standard HTTP methods (GET, POST, PUT, DELETE)
    //   • The Authorization header (needed to send the JWT token)

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, Authorization header)
        config.setAllowCredentials(true);

        // Origins allowed to call the API
        // In production, replace with your actual domain e.g. "https://yourdomain.com"
        config.setAllowedOrigins(List.of(
                "http://localhost:8080",
                "http://localhost:3000",   // if using a separate frontend dev server
                "http://127.0.0.1:8080"
        ));

        // Allow the Authorization header so JWT tokens can be sent
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept"
        ));

        // Allow all standard REST methods
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Cache preflight response for 1 hour (reduces OPTIONS requests)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);  // apply to all API routes

        return new CorsFilter(source);
    }
}