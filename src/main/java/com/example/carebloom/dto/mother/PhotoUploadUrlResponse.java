package com.example.carebloom.dto.mother;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing photo upload URL and details")
public class PhotoUploadUrlResponse {
    
    @Schema(description = "Signed URL for uploading the photo", 
            example = "https://storage.googleapis.com/carebloom-profile-photos/profiles/user123/profile_image.jpg?signature=...")
    private String uploadUrl;
    
    @Schema(description = "Unique identifier for this upload session", 
            example = "upload_1696284567890_abc123")
    private String photoId;
    
    @Schema(description = "Expiration time of the upload URL", 
            example = "2024-10-03T15:30:00")
    private LocalDateTime expiresAt;
}