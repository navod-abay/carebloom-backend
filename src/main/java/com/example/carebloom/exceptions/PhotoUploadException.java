package com.example.carebloom.exceptions;

public class PhotoUploadException extends RuntimeException {
    
    public PhotoUploadException(String message) {
        super(message);
    }
    
    public PhotoUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}