package com.example.carebloom.dto.vendor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO after updating order status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateResponseDto {
    private boolean success;
    private String message;
    private int itemsUpdated; // Number of cart items updated
    private String orderId;
    private String newStatus;
}
