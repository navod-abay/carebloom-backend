package com.example.carebloom.controllers.vendor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.carebloom.dto.vendor.VendorOrderDto;
import com.example.carebloom.services.vendors.VendorCartService;

@RestController
@CrossOrigin(origins = "${app.cors.vendor-origin}", allowCredentials = "true")
@RequestMapping("/api/v1/vendors/{vendorId}/orders")
public class VendorCartController {

    private static final Logger logger = LoggerFactory.getLogger(VendorCartController.class);

    @Autowired
    private VendorCartService vendorCartService;

    @GetMapping
    public List<VendorOrderDto> getOrdersForVendor(@PathVariable String vendorId) {
        logger.debug("Fetching vendor orders for vendorId={}", vendorId);
        List<VendorOrderDto> orders = vendorCartService.getOrdersForVendor(vendorId);
        logger.debug("Found {} vendor orders for vendorId={}", orders == null ? 0 : orders.size(), vendorId);
        return orders;
    }
}
