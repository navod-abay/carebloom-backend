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
        logger.info("=== MONTHLY CUSTOMER RATE ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getMonthlyCustomerRate()...");
            Map<String, Object> result = vendorDashboardService.getMonthlyCustomerRate(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Current year total: {}", result.get("currentYearTotal"));
            logger.info("Previous year total: {}", result.get("previousYearTotal"));
            logger.info("Percentage change: {}%", result.get("percentageChange"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getMonthlyCustomerRate: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get monthly customer rate", "details", e.getMessage()));
        }
    }

    @GetMapping("/month/items-sold")
    public ResponseEntity<?> getMonthlyItemsSold() {
        logger.info("=== MONTHLY ITEMS SOLD ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getMonthlyItemsSold()...");
            Map<String, Object> result = vendorDashboardService.getMonthlyItemsSold(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Current year total: {}", result.get("currentYearTotal"));
            logger.info("Previous year total: {}", result.get("previousYearTotal"));
            logger.info("Percentage change: {}%", result.get("percentageChange"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getMonthlyItemsSold: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get monthly items sold", "details", e.getMessage()));
        }
    }

    @GetMapping("/month/revenue")
    public ResponseEntity<?> getMonthlyRevenue() {
        logger.info("=== MONTHLY REVENUE ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getMonthlyRevenue()...");
            Map<String, Object> result = vendorDashboardService.getMonthlyRevenue(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Current year total: {}", result.get("currentYearTotal"));
            logger.info("Previous year total: {}", result.get("previousYearTotal"));
            logger.info("Percentage change: {}%", result.get("percentageChange"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getMonthlyRevenue: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get monthly revenue", "details", e.getMessage()));
        }
    }

    @GetMapping("/month/order-status")
    public ResponseEntity<?> getMonthlyOrderStatus() {
        logger.info("=== MONTHLY ORDER STATUS ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getMonthlyOrderStatus()...");
            Map<String, Object> result = vendorDashboardService.getMonthlyOrderStatus(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Current year total: {}", result.get("currentYearTotal"));
            logger.info("Previous year total: {}", result.get("previousYearTotal"));
            logger.info("Percentage change: {}%", result.get("percentageChange"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getMonthlyOrderStatus: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get monthly order status", "details", e.getMessage()));
        }
    }

    // Yearly analytics endpoints (month comparison)
    @GetMapping("/year/customer-rate")
    public ResponseEntity<?> getYearlyCustomerRate() {
        logger.info("=== YEARLY CUSTOMER RATE ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getYearlyCustomerRate()...");
            Map<String, Object> result = vendorDashboardService.getYearlyCustomerRate(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Current month: {}, Previous month: {}", 
                       result.get("currentMonth"), result.get("previousMonth"));
            logger.info("Current month total: {}", result.get("currentMonthTotal"));
            logger.info("Previous month total: {}", result.get("previousMonthTotal"));
            logger.info("Percentage change: {}%", result.get("percentageChange"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getYearlyCustomerRate: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get yearly customer rate", "details", e.getMessage()));
        }
    }

    @GetMapping("/year/items-sold")
    public ResponseEntity<?> getYearlyItemsSold() {
        logger.info("=== YEARLY ITEMS SOLD ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getYearlyItemsSold()...");
            Map<String, Object> result = vendorDashboardService.getYearlyItemsSold(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Current month: {}, Previous month: {}", 
                       result.get("currentMonth"), result.get("previousMonth"));
            logger.info("Current month total: {}", result.get("currentMonthTotal"));
            logger.info("Previous month total: {}", result.get("previousMonthTotal"));
            logger.info("Percentage change: {}%", result.get("percentageChange"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getYearlyItemsSold: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get yearly items sold", "details", e.getMessage()));
        }
    }

    @GetMapping("/year/revenue")
    public ResponseEntity<?> getYearlyRevenue() {
        logger.info("=== YEARLY REVENUE ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getYearlyRevenue()...");
            Map<String, Object> result = vendorDashboardService.getYearlyRevenue(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Current month: {}, Previous month: {}", 
                       result.get("currentMonth"), result.get("previousMonth"));
            logger.info("Current month total: {}", result.get("currentMonthTotal"));
            logger.info("Previous month total: {}", result.get("previousMonthTotal"));
            logger.info("Percentage change: {}%", result.get("percentageChange"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getYearlyRevenue: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get yearly revenue", "details", e.getMessage()));
        }
    }

    @GetMapping("/year/order-status")
    public ResponseEntity<?> getYearlyOrderStatus() {
        logger.info("=== YEARLY ORDER STATUS ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getYearlyOrderStatus()...");
            Map<String, Object> result = vendorDashboardService.getYearlyOrderStatus(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Current month: {}, Previous month: {}", 
                       result.get("currentMonth"), result.get("previousMonth"));
            logger.info("Current month total: {}", result.get("currentMonthTotal"));
            logger.info("Previous month total: {}", result.get("previousMonthTotal"));
            logger.info("Percentage change: {}%", result.get("percentageChange"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getYearlyOrderStatus: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get yearly order status", "details", e.getMessage()));
        }
    }

    // Additional endpoint for detailed product status breakdown
    @GetMapping("/product-status")
    public ResponseEntity<?> getProductStatusBreakdown() {
        logger.info("=== PRODUCT STATUS BREAKDOWN ENDPOINT CALLED ===");
        try {
            Vendor currentVendor = SecurityUtils.getCurrentVendor();
            logger.info("Authenticated vendor: ID={}, BusinessName={}", 
                       currentVendor.getId(), currentVendor.getBusinessName());

            logger.info("Calling VendorDashboardService.getProductStatusBreakdown()...");
            Map<String, Object> result = vendorDashboardService.getProductStatusBreakdown(currentVendor.getId());
            
            logger.info("Service response received. Keys: {}", result.keySet());
            logger.info("Product status breakdown result: {}", result);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("ERROR in getProductStatusBreakdown: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get product status breakdown", "details", e.getMessage()));
        }
    }
}
