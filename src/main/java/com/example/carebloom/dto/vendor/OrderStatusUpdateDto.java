package com.example.carebloom.dto.vendor;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating order status from vendor dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdateDto {
    private String orderId; // The generated order ID (UUID from grouped cart items)
    private String newStatus; // pending, confirmed, cancelled, delivered
    private LocalDateTime deliveryDate; // For delivered status
    private String cancellationReason; // For cancelled status
    private String trackingNumber; // For confirmed/processing status
    private String deliveryNotes; // Additional notes
}
