package com.example.carebloom.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    @Field("vendor_id")
    private String vendorId;

    @Field("customer_id")
    private String customerId; // Firebase UID of the customer (mother)

    @Field("customer_name")
    private String customerName;

    @Field("order_items")
    private List<OrderItem> orderItems;

    @Field("total_amount")
    private BigDecimal totalAmount;

    @Field("status")
    private OrderStatus status = OrderStatus.PENDING;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    // Embedded class for order items
    @Data
    public static class OrderItem {
        @Field("product_id")
        private String productId;

        @Field("product_name")
        private String productName;

        @Field("quantity")
        private Integer quantity;

        @Field("vendor_id")
        private String vendorId;
    }

    // Enum for order status
    public enum OrderStatus {
        PENDING,
        PROCESSING,
        READY,
        CANCELLED
    }
}