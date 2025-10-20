package com.example.carebloom.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cart_items")
@CompoundIndex(name = "user_product_idx", def = "{'userId': 1, 'productId': 1}", unique = true)
public class CartItem {
    @Id
    private String id;
    
    @Indexed
    private String userId; // Mother's Firebase UID
    private String productId; // Reference to Product
    private String vendorId; // Reference to vendor for easier queries
    private Integer quantity;
    private Double priceAtAdd; // Price when added to cart (for price changes)
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
    
    // Order management fields
    private String orderStatus; // pending, confirmed, cancelled, delivered
    private String paymentStatus; // pending, paid, failed, refunded
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String trackingNumber;
    private String deliveryNotes;
}