package com.example.carebloom.dto.vendor;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderDto {
    private String orderId; // cart item group id or generated id
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderDate;
    private String orderStatus; // pending/confirmed/processing/delivered/cancelled
    private String paymentStatus; // pending/paid/failed/refunded
    private String paymentMethod; // COD
    private Double subtotal;
    private Double shippingCost;
    private Double tax;
    private Double totalAmount;
    private String priorityLevel;
    private List<VendorOrderItemDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VendorOrderItemDto {
        private String productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
        private String productImage;
    }
}
