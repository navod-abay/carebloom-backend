package com.example.carebloom.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Configuration
public class GoogleCloudConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudConfig.class);
    
    @Value("${app.gcs.credentials-path:firebase-service-account.json}")
    private String credentialsPath;
    
    @Value("${app.gcs.project-id:}")
    private String projectId;
    
    @Bean
    public Storage googleCloudStorage() throws IOException {
        try {
            // Load credentials from classpath
            ClassPathResource resource = new ClassPathResource(credentialsPath);
            GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
            
            StorageOptions.Builder builder = StorageOptions.newBuilder()
                .setCredentials(credentials);
                
            // Set project ID if provided
            if (projectId != null && !projectId.isEmpty()) {
                builder.setProjectId(projectId);
            }
            
            Storage storage = builder.build().getService();
            logger.info("Google Cloud Storage initialized successfully");
            return storage;
            
        } catch (Exception e) {
            logger.error("Failed to initialize Google Cloud Storage: {}", e.getMessage());
            throw new IOException("Failed to initialize Google Cloud Storage", e);
        }
    }
}