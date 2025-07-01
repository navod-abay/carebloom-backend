package com.example.carebloom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Autowired
    private CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Admin endpoints
        registerCorsConfig(source, "/api/v1/admin/**", corsProperties.getAdminOrigin());
        
        // Mother endpoints
        registerCorsConfig(source, "/api/v1/mother/**", corsProperties.getMotherOrigin());
        
        // Midwife endpoints
        registerCorsConfig(source, "/api/v1/midwife/**", corsProperties.getMidwifeOrigin());
        
        // Vendor endpoints
        registerCorsConfig(source, "/api/v1/vendor/**", corsProperties.getVendorOrigin());
        
        // MOH Office endpoints
        registerCorsConfig(source, "/api/v1/moh/**", corsProperties.getMohOrigin());
        
        return source;
    }
    
    private void registerCorsConfig(UrlBasedCorsConfigurationSource source, String path, String origin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origin));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration(path, config);
    }
}
