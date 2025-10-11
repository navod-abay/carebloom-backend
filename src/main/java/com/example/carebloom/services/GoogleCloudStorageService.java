package com.example.carebloom.services;

import com.google.cloud.storage.*;
import com.google.cloud.storage.Storage.SignUrlOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class GoogleCloudStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudStorageService.class);
    
    @Value("${app.gcs.bucket-name:carebloom-profile-photos}")
    private String bucketName;
    
    @Value("${app.photo.max-size-mb:5}")
    private long maxSizeMB;
    
    private final Storage storage;
    
    // Pattern for allowed file types
    private static final Pattern ALLOWED_FILE_PATTERN = Pattern.compile(".*\\.(jpg|jpeg|png)$", Pattern.CASE_INSENSITIVE);
    
    public GoogleCloudStorageService(Storage storage) {
        this.storage = storage;
    }
    
    /**
     * Generate a signed URL for uploading a photo
     */
    public URL generateUploadUrl(String userId, String fileName) {
        validateFileName(fileName);
        
        String sanitizedFileName = sanitizeFileName(fileName);
        String objectName = String.format("profiles/%s/%s", userId, sanitizedFileName);
        
        logger.info("Generating upload URL for object: {}", objectName);
        
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(getContentType(fileName))
                .build();
        
        // Generate signed URL valid for 15 minutes
        URL signedUrl = storage.signUrl(
                blobInfo,
                15, TimeUnit.MINUTES,
                SignUrlOption.httpMethod(HttpMethod.PUT),
                SignUrlOption.withV4Signature()
        );
        
        logger.info("Generated signed URL for upload, expires in 15 minutes");
        return signedUrl;
    }
    
    /**
     * Check if a file exists in GCS
     */
    public boolean fileExists(String userId, String fileName) {
        String objectName = String.format("profiles/%s/%s", userId, fileName);
        logger.info("Checking if file exists in GCS: {}", objectName);
        Blob blob = storage.get(bucketName, objectName);
        return blob != null && blob.exists();
    }
    
    /**
     * Get public URL for a uploaded file
     */
    public String getPublicUrl(String userId, String fileName) {
        String sanitizedFileName = sanitizeFileName(fileName);
        String objectName = String.format("profiles/%s/%s", userId, sanitizedFileName);
        
        return String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
    }
    
    /**
     * Delete a file from GCS
     */
    public boolean deleteFile(String userId, String fileName) {
        try {
            String sanitizedFileName = sanitizeFileName(fileName);
            String objectName = String.format("profiles/%s/%s", userId, sanitizedFileName);
            
            boolean deleted = storage.delete(bucketName, objectName);
            
            if (deleted) {
                logger.info("Successfully deleted file: {}", objectName);
            } else {
                logger.warn("File not found for deletion: {}", objectName);
            }
            
            return deleted;
        } catch (Exception e) {
            logger.error("Error deleting file for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
   
    /**
     * Validate file name only (file size validation handled by frontend)
     */
    private void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }
        
        if (!ALLOWED_FILE_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException("Only JPG and PNG files are allowed");
        }
    }
    
    /**
     * Sanitize file name to prevent path traversal and other security issues
     * Returns consistent filename for the same input
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "profile_image.jpg";
        }
        
        // Remove any path separators and dangerous characters
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Ensure it's not empty after sanitization
        if (sanitized.trim().isEmpty()) {
            sanitized = "profile_image.jpg";
        }
        
        // Just return the sanitized filename without timestamp
        return sanitized;
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg"; // default extension
    }
    
    /**
     * Get content type based on file extension
     */
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension.toLowerCase()) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            default:
                return "image/jpeg";
        }
    }
}