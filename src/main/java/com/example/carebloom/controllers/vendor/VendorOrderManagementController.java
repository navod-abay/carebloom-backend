package com.example.carebloom.controllers.vendor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.carebloom.dto.vendor.OrderStatusUpdateDto;
import com.example.carebloom.dto.vendor.OrderStatusUpdateResponseDto;
import com.example.carebloom.services.vendors.VendorOrderManagementService;

/**
 * REST Controller for vendor order management (status updates, delivery tracking, etc.)
 */
@RestController
@CrossOrigin(origins = "${app.cors.vendor-origin}", allowCredentials = "true")
@RequestMapping("/api/v1/vendors/{vendorId}/orders")
public class VendorOrderManagementController {

    private static final Logger logger = LoggerFactory.getLogger(VendorOrderManagementController.class);

    @Autowired
    private VendorOrderManagementService orderManagementService;

    /**
     * Update order status (confirm, cancel, mark as delivered, etc.)
     * POST /api/v1/vendors/{vendorId}/orders/update-status
     */
    @PostMapping("/update-status")
    public ResponseEntity<OrderStatusUpdateResponseDto> updateOrderStatus(
            @PathVariable String vendorId,
            @RequestBody OrderStatusUpdateDto updateDto) {
        
        logger.info("Vendor {} updating order {} to status {}", 
            vendorId, updateDto.getOrderId(), updateDto.getNewStatus());
        
        OrderStatusUpdateResponseDto response = orderManagementService.updateOrderStatus(vendorId, updateDto);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
