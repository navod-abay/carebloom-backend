package com.example.carebloom.dto.product;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private String id;
    private String vendorId;
    private String name;
    private String category;
    private Integer sectionId;
    private Double price;
    private Integer stock;
    private String status;
    private String description;
    private String imageUrl;
    private String sku;
    private Double weight;
    private String dimensions;
    private Integer lowStockThreshold;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
