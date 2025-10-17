package com.example.carebloom.controllers;

import com.example.carebloom.dto.cart.AddToCartRequest;
import com.example.carebloom.dto.cart.CartItemResponse;
import com.example.carebloom.services.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('MOTHER')")
    public ResponseEntity<CartItemResponse> addToCart(
            Authentication authentication,
            @RequestBody AddToCartRequest request) {
        
        try {
            String userId = authentication.getName(); // Get Firebase UID from authentication
            CartItemResponse response = cartService.addToCart(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid add to cart request", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error adding to cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user's cart items
     */
    @GetMapping
    @PreAuthorize("hasRole('MOTHER')")
    public ResponseEntity<List<CartItemResponse>> getCartItems(Authentication authentication) {
        try {
            String userId = authentication.getName(); // Get Firebase UID from authentication
            List<CartItemResponse> cartItems = cartService.getCartItems(userId);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            logger.error("Error getting cart items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update cart item quantity
     */
    @PutMapping("/{cartItemId}")
    @PreAuthorize("hasRole('MOTHER')")
    public ResponseEntity<CartItemResponse> updateCartItem(
            Authentication authentication,
            @PathVariable String cartItemId,
            @RequestBody Map<String, Integer> request) {
        
        try {
            String userId = authentication.getName();
            Integer quantity = request.get("quantity");
            
            if (quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            CartItemResponse response = cartService.updateCartItem(userId, cartItemId, quantity);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating cart item", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Remove item from cart
     */
    @DeleteMapping("/{cartItemId}")
    @PreAuthorize("hasRole('MOTHER')")
    public ResponseEntity<Void> removeFromCart(
            Authentication authentication,
            @PathVariable String cartItemId) {
        
        try {
            String userId = authentication.getName();
            cartService.removeFromCart(userId, cartItemId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error removing from cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clear cart
     */
    @DeleteMapping
    @PreAuthorize("hasRole('MOTHER')")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        try {
            String userId = authentication.getName();
            cartService.clearCart(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error clearing cart", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cart item count
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('MOTHER')")
    public ResponseEntity<Map<String, Long>> getCartItemCount(Authentication authentication) {
        try {
            String userId = authentication.getName();
            long count = cartService.getCartItemCount(userId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            logger.error("Error getting cart count", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}