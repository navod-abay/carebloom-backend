package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    
    private String vendorId; // Reference to the vendor who owns this product
    private String name;
    private String category;
    // Section / sub-category id from mobile marketplace inventory (e.g., 101, 201, ...)
    private Integer sectionId;
    private Double price;
    private Integer stock;
    private String status; // active, inactive, out-of-stock
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for better product management
    private String sku; // Stock Keeping Unit
    private Double weight;
    private String dimensions;
    private Integer lowStockThreshold;
    private Boolean isActive;
}
