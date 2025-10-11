package com.example.carebloom.dto.mother;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request for generating photo upload URL")
public class PhotoUploadUrlRequest {
    
    @Schema(description = "Name of the file to upload", example = "profile_image.jpg")
    @NotBlank(message = "File name is required")
    @Size(max = 100, message = "File name must not exceed 100 characters")
    private String fileName;
}