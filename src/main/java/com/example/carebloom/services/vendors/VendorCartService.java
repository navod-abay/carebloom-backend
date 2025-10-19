package com.example.carebloom.services.vendors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.carebloom.dto.vendor.VendorOrderDto;
import com.example.carebloom.models.CartItem;
import com.example.carebloom.models.Mother;
import com.example.carebloom.models.Product;
import com.example.carebloom.repositories.CartRepository;
import com.example.carebloom.repositories.MotherRepository;
import com.example.carebloom.repositories.ProductRepository;

@Service
public class VendorCartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MotherRepository motherRepository;

    /**
     * Fetch vendor orders (derived from cart_items) and map to DTOs.
     * This implementation groups cart items by user (customer) and orderDate (addedAt) into simple vendor orders.
     * It returns minimal fields needed by the vendor UI.
     */
    public List<VendorOrderDto> getOrdersForVendor(String vendorId) {
        // TEMPORARY: Fetch all cart items (not filtering by vendorId) to show all orders
        // TODO: Ensure cart items in DB have vendorId populated, then filter properly
        List<CartItem> items = cartRepository.findAll();
        if (items == null || items.isEmpty()) return new ArrayList<>();

        // Load products
        List<String> productIds = items.stream().map(CartItem::getProductId).distinct().collect(Collectors.toList());
        List<Product> products = productRepository.findAllById(productIds);
        Map<String, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        // Group items into simple orders by userId and date bucket (day)
        Map<String, List<CartItem>> grouped = items.stream()
                .collect(Collectors.groupingBy(ci -> ci.getUserId() + "::" + ci.getAddedAt().toLocalDate().toString()));

        List<VendorOrderDto> orders = new ArrayList<>();

        for (Map.Entry<String, List<CartItem>> entry : grouped.entrySet()) {
            List<CartItem> groupItems = entry.getValue();
            VendorOrderDto order = new VendorOrderDto();
            // Use first item as representative
            CartItem first = groupItems.get(0);

            // Create a deterministic orderId for the grouped cart items using the group key
            // group key format: userId::YYYY-MM-DD
            String groupKey = entry.getKey();
            String orderId = UUID.nameUUIDFromBytes(groupKey.getBytes()).toString();
            order.setOrderId(orderId);
            order.setCustomerId(first.getUserId());
            order.setOrderDate(first.getAddedAt() != null ? first.getAddedAt() : LocalDateTime.now());
            order.setOrderStatus("pending");
            order.setPaymentStatus("pending");
            order.setPaymentMethod("Cash on Delivery");
            order.setPriorityLevel("standard");

            double subtotal = 0.0;
            List<VendorOrderDto.VendorOrderItemDto> dtoItems = new ArrayList<>();

            for (CartItem ci : groupItems) {
                Product p = productMap.get(ci.getProductId());
                VendorOrderDto.VendorOrderItemDto it = new VendorOrderDto.VendorOrderItemDto();
                it.setProductId(ci.getProductId());
                it.setQuantity(ci.getQuantity());
                it.setUnitPrice(ci.getPriceAtAdd());
                it.setTotalPrice(ci.getPriceAtAdd() * ci.getQuantity());

                if (p != null) {
                    it.setProductName(p.getName());
                    it.setProductSku(p.getSku());
                    it.setProductImage(p.getImageUrl());
                }

                dtoItems.add(it);
                subtotal += it.getTotalPrice();
            }

            order.setItems(dtoItems);
            order.setSubtotal(subtotal);
            order.setShippingCost(0.0);
            order.setTax(0.0);
            order.setTotalAmount(subtotal);

            // Try to populate customer info from mothers collection using the Firebase UID stored in cart userId
            try {
                if (first.getUserId() != null) {
                    Mother m = motherRepository.findByFirebaseUid(first.getUserId());
                    if (m != null) {
                        order.setCustomerName(m.getName());
                        order.setCustomerEmail(m.getEmail());
                        order.setCustomerPhone(m.getPhone());
                    } else {
                        order.setCustomerName(null);
                        order.setCustomerEmail(null);
                        order.setCustomerPhone(null);
                    }
                }
            } catch (Exception ex) {
                // On any issue fetching mother profile, leave customer fields null but continue returning orders
                order.setCustomerName(null);
                order.setCustomerEmail(null);
                order.setCustomerPhone(null);
            }

            orders.add(order);
        }

        return orders;
    }
}
