package com.example.carebloom.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Data
public class CorsProperties {
    private String adminOrigin;
    private String motherOrigin;
    private String midwifeOrigin;
    private String vendorOrigin;
    private String mohOrigin;
}
