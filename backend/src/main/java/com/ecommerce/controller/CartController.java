package com.ecommerce.controller;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Shopping cart management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get customer's cart", description = "Retrieve all items in the customer's shopping cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CartDto>> getCart(Authentication authentication) {
        try {
            Long customerId = Long.parseLong(authentication.getName());
            CartDto cart = cartService.getCart(customerId);
            
            return ResponseEntity.ok(ApiResponse.<CartDto>builder()
                    .success(true)
                    .message("Cart retrieved successfully")
                    .data(cart)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error retrieving cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CartDto>builder()
                            .success(false)
                            .message("Failed to retrieve cart: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Add a product to the customer's shopping cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Item added to cart successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CartItemDto>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        try {
            Long customerId = Long.parseLong(authentication.getName());
            CartItemDto cartItem = cartService.addToCart(customerId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<CartItemDto>builder()
                            .success(true)
                            .message("Item added to cart successfully")
                            .data(cartItem)
                            .build());
                            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for adding to cart: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<CartItemDto>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error adding item to cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CartItemDto>builder()
                            .success(false)
                            .message("Failed to add item to cart: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/items/{productId}")
    @Operation(summary = "Update cart item", description = "Update the quantity of an item in the cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<CartItemDto>> updateCartItem(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        try {
            Long customerId = Long.parseLong(authentication.getName());
            CartItemDto cartItem = cartService.updateCartItem(customerId, productId, request);
            
            return ResponseEntity.ok(ApiResponse.<CartItemDto>builder()
                    .success(true)
                    .message("Cart item updated successfully")
                    .data(cartItem)
                    .build());
                    
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for updating cart item: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<CartItemDto>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error updating cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<CartItemDto>builder()
                            .success(false)
                            .message("Failed to update cart item: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/items/{productId}")
    @Operation(summary = "Remove item from cart", description = "Remove a specific item from the cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item removed from cart successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cart item not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            Authentication authentication) {
        try {
            Long customerId = Long.parseLong(authentication.getName());
            cartService.removeFromCart(customerId, productId);
            
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Item removed from cart successfully")
                    .build());
                    
        } catch (Exception e) {
            log.error("Error removing item from cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to remove item from cart: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Remove all items from the customer's cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        try {
            Long customerId = Long.parseLong(authentication.getName());
            cartService.clearCart(customerId);
            
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Cart cleared successfully")
                    .build());
                    
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to clear cart: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/count")
    @Operation(summary = "Get cart item count", description = "Get the total number of items in the customer's cart")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart count retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(Authentication authentication) {
        try {
            Long customerId = Long.parseLong(authentication.getName());
            int count = cartService.getCartItemCount(customerId);
            
            return ResponseEntity.ok(ApiResponse.<Integer>builder()
                    .success(true)
                    .message("Cart count retrieved successfully")
                    .data(count)
                    .build());
                    
        } catch (Exception e) {
            log.error("Error retrieving cart count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Integer>builder()
                            .success(false)
                            .message("Failed to retrieve cart count: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate cart items", description = "Validate cart items against current product stock and prices")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cart validated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cart validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Void>> validateCart(Authentication authentication) {
        try {
            Long customerId = Long.parseLong(authentication.getName());
            cartService.validateCartItems(customerId);
            
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .success(true)
                    .message("Cart validated successfully")
                    .build());
                    
        } catch (IllegalArgumentException e) {
            log.warn("Cart validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error validating cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Void>builder()
                            .success(false)
                            .message("Failed to validate cart: " + e.getMessage())
                            .build());
        }
    }
}