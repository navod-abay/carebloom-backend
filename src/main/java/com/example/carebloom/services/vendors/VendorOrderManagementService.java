package com.example.carebloom.services.vendors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.carebloom.dto.vendor.OrderStatusUpdateDto;
import com.example.carebloom.dto.vendor.OrderStatusUpdateResponseDto;
import com.example.carebloom.models.CartItem;
import com.example.carebloom.repositories.CartRepository;

/**
 * Service for managing vendor orders (updating status, delivery dates, etc.)
 */
@Service
public class VendorOrderManagementService {

    private static final Logger logger = LoggerFactory.getLogger(VendorOrderManagementService.class);

    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private com.example.carebloom.repositories.ProductRepository productRepository;

    /**
     * Update order status for a vendor order.
     * Since orders are grouped cart items, we need to find all cart items that belong to this order
     * and update their status fields.
     */
    @Transactional
    public OrderStatusUpdateResponseDto updateOrderStatus(String vendorId, OrderStatusUpdateDto updateDto) {
        try {
            // Find all cart items for this vendor
            List<CartItem> allVendorItems = cartRepository.findAll();
            
            // Group by userId + date to recreate the orderId
            List<CartItem> orderItems = new ArrayList<>();
            
            for (CartItem item : allVendorItems) {
                if (item.getAddedAt() == null) continue;
                
                String groupKey = item.getUserId() + "::" + item.getAddedAt().toLocalDate().toString();
                String orderId = UUID.nameUUIDFromBytes(groupKey.getBytes()).toString();
                
                if (orderId.equals(updateDto.getOrderId())) {
                    orderItems.add(item);
                }
            }
            
            if (orderItems.isEmpty()) {
                return new OrderStatusUpdateResponseDto(
                    false, 
                    "Order not found", 
                    0, 
                    updateDto.getOrderId(), 
                    updateDto.getNewStatus()
                );
            }
            
            // Update all cart items in this order
            LocalDateTime now = LocalDateTime.now();
            for (CartItem item : orderItems) {
                item.setOrderStatus(updateDto.getNewStatus());
                item.setUpdatedAt(now);
                
                // Set timestamps based on status
                switch (updateDto.getNewStatus().toLowerCase()) {
                    case "confirmed":
                        item.setConfirmedAt(now);
                        break;
                    case "delivered":
                        item.setDeliveredAt(updateDto.getDeliveryDate() != null ? updateDto.getDeliveryDate() : now);
                        break;
                    case "cancelled":
                        item.setCancelledAt(now);
                        item.setCancellationReason(updateDto.getCancellationReason());
                        
                        // Restore stock when order is cancelled
                        try {
                            java.util.Optional<com.example.carebloom.models.Product> productOpt = 
                                productRepository.findById(item.getProductId());
                            if (productOpt.isPresent()) {
                                com.example.carebloom.models.Product product = productOpt.get();
                                product.setStock(product.getStock() + item.getQuantity());
                                productRepository.save(product);
                                logger.info("Restored {} units to product {} stock due to order cancellation. New stock: {}", 
                                    item.getQuantity(), product.getId(), product.getStock());
                            }
                        } catch (Exception e) {
                            logger.error("Failed to restore stock for cancelled item", e);
                        }
                        break;
                }
                
                // Update tracking and notes
                if (updateDto.getTrackingNumber() != null) {
                    item.setTrackingNumber(updateDto.getTrackingNumber());
                }
                if (updateDto.getDeliveryNotes() != null) {
                    item.setDeliveryNotes(updateDto.getDeliveryNotes());
                }
            }
            
            // Save all updated items
            cartRepository.saveAll(orderItems);
            
            logger.info("Updated order {} status to {} ({} items)", 
                updateDto.getOrderId(), updateDto.getNewStatus(), orderItems.size());
            
            return new OrderStatusUpdateResponseDto(
                true,
                "Order status updated successfully",
                orderItems.size(),
                updateDto.getOrderId(),
                updateDto.getNewStatus()
            );
            
        } catch (Exception e) {
            logger.error("Error updating order status", e);
            return new OrderStatusUpdateResponseDto(
                false,
                "Error updating order: " + e.getMessage(),
                0,
                updateDto.getOrderId(),
                updateDto.getNewStatus()
            );
        }
    }
    
    /**
     * Get order details by orderId
     */
    public List<CartItem> getOrderItems(String orderId) {
        List<CartItem> allItems = cartRepository.findAll();
        List<CartItem> orderItems = new ArrayList<>();
        
        for (CartItem item : allItems) {
            if (item.getAddedAt() == null) continue;
            
            String groupKey = item.getUserId() + "::" + item.getAddedAt().toLocalDate().toString();
            String itemOrderId = UUID.nameUUIDFromBytes(groupKey.getBytes()).toString();
            
            if (itemOrderId.equals(orderId)) {
                orderItems.add(item);
            }
        }
        
        return orderItems;
    }
}
