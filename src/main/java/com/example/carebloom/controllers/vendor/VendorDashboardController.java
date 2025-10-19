package com.example.carebloom.controllers.vendor;

import com.example.carebloom.services.vendors.VendorDashboardService;
import com.example.carebloom.utils.SecurityUtils;
import com.example.carebloom.models.Vendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/vendor")
@CrossOrigin(origins = "${app.cors.vendor-origin}")
public class VendorDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(VendorDashboardController.class);

    @Autowired
    private VendorDashboardService vendorDashboardService;

    // Monthly analytics endpoints (year comparison)
    @GetMapping("/month/customer-rate")
    public ResponseEntity<?> getMonthlyCustomerRate() {
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Getting monthly customer rate for vendor: {}", currentVendor.getId());

            Map<String, Object> result = vendorDashboardService.getMonthlyCustomerRate(currentVendor.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting monthly customer rate: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get monthly customer rate"));
        }
    }

    @GetMapping("/month/items-sold")
    public ResponseEntity<?> getMonthlyItemsSold() {
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Getting monthly items sold for vendor: {}", currentVendor.getId());

            Map<String, Object> result = vendorDashboardService.getMonthlyItemsSold(currentVendor.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting monthly items sold: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get monthly items sold"));
        }
    }

    @GetMapping("/month/revenue")
    public ResponseEntity<?> getMonthlyRevenue() {
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Getting monthly revenue for vendor: {}", currentVendor.getId());

            Map<String, Object> result = vendorDashboardService.getMonthlyRevenue(currentVendor.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting monthly revenue: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get monthly revenue"));
        }
    }

    @GetMapping("/month/order-status")
    public ResponseEntity<?> getMonthlyOrderStatus() {
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Getting monthly order status for vendor: {}", currentVendor.getId());

            Map<String, Object> result = vendorDashboardService.getMonthlyOrderStatus(currentVendor.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting monthly order status: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get monthly order status"));
        }
    }

    // Yearly analytics endpoints (month comparison)
    @GetMapping("/year/customer-rate")
    public ResponseEntity<?> getYearlyCustomerRate() {
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Getting yearly customer rate for vendor: {}", currentVendor.getId());

            Map<String, Object> result = vendorDashboardService.getYearlyCustomerRate(currentVendor.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting yearly customer rate: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get yearly customer rate"));
        }
    }

    @GetMapping("/year/items-sold")
    public ResponseEntity<?> getYearlyItemsSold() {
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Getting yearly items sold for vendor: {}", currentVendor.getId());

            Map<String, Object> result = vendorDashboardService.getYearlyItemsSold(currentVendor.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting yearly items sold: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get yearly items sold"));
        }
    }

    @GetMapping("/year/revenue")
    public ResponseEntity<?> getYearlyRevenue() {
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Getting yearly revenue for vendor: {}", currentVendor.getId());

            Map<String, Object> result = vendorDashboardService.getYearlyRevenue(currentVendor.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting yearly revenue: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get yearly revenue"));
        }
    }

    @GetMapping("/year/order-status")
    public ResponseEntity<?> getYearlyOrderStatus() {
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Getting yearly order status for vendor: {}", currentVendor.getId());

            Map<String, Object> result = vendorDashboardService.getYearlyOrderStatus(currentVendor.getId());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting yearly order status: ", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get yearly order status"));
        }
    }
}
