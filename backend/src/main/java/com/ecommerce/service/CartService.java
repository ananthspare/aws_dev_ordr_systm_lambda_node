package com.ecommerce.service;

import com.ecommerce.dto.AddToCartRequest;
import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.dynamodb.CartItem;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.dynamodb.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    /**
     * Get cart for a customer
     */
    public CartDto getCart(Long customerId) {
        log.info("Getting cart for customer: {}", customerId);
        
        List<CartItem> cartItems = cartRepository.findByCustomerId(String.valueOf(customerId));
        
        List<CartItemDto> itemDtos = cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        Integer totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String lastUpdated = cartItems.stream()
                .map(CartItem::getUpdatedAt)
                .max(String::compareTo)
                .orElse(Instant.now().toString());
        
        return CartDto.builder()
                .customerId(customerId)
                .items(itemDtos)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .lastUpdated(lastUpdated)
                .build();
    }

    /**
     * Add item to cart
     */
    @Transactional
    public CartItemDto addToCart(Long customerId, AddToCartRequest request) {
        log.info("Adding item to cart for customer: {}, productId: {}, quantity: {}", 
                customerId, request.getProductId(), request.getQuantity());
        
        // Validate product exists and has sufficient stock
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        
        if (product.getStockQty() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQty());
        }
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartRepository.findByCustomerIdAndProductId(String.valueOf(customerId), request.getProductId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update existing item
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            if (product.getStockQty() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQty() + 
                        ", Requested total: " + newQuantity);
            }
            
            cartItem.setQuantity(newQuantity);
            // Update product details in case they changed
            cartItem.setProductName(product.getName());
            cartItem.setImageUrl(product.getImageUrl());
            cartItem.setUnitPrice(product.getPrice());
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                    .customerId(String.valueOf(customerId))
                    .productId(request.getProductId())
                    .itemKey("PRODUCT#" + request.getProductId())
                    .productName(product.getName())
                    .imageUrl(product.getImageUrl())
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
        }
        
        // Calculate total price and set timestamps
        cartItem.calculateTotalPrice();
        cartItem.setTimestamps();
        
        CartItem savedItem = cartRepository.save(cartItem);
        return convertToDto(savedItem);
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartItemDto updateCartItem(Long customerId, Long productId, UpdateCartItemRequest request) {
        log.info("Updating cart item for customer: {}, productId: {}, quantity: {}", 
                customerId, productId, request.getQuantity());
        
        // Get existing cart item
        CartItem cartItem = cartRepository.findByCustomerIdAndProductId(String.valueOf(customerId), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        // Validate product stock
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        if (product.getStockQty() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStockQty());
        }
        
        cartItem.setQuantity(request.getQuantity());
        cartItem.setUnitPrice(product.getPrice()); // Update price in case it changed
        cartItem.setProductName(product.getName()); // Update name in case it changed
        cartItem.setImageUrl(product.getImageUrl()); // Update image URL in case it changed
        
        // Calculate total price and set timestamps
        cartItem.calculateTotalPrice();
        cartItem.setTimestamps();
        
        CartItem savedItem = cartRepository.save(cartItem);
        return convertToDto(savedItem);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public void removeFromCart(Long customerId, Long productId) {
        log.info("Removing item from cart for customer: {}, productId: {}", customerId, productId);
        
        // Verify item exists
        cartRepository.findByCustomerIdAndProductId(String.valueOf(customerId), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        cartRepository.deleteByCustomerIdAndProductId(customerId, productId);
    }

    /**
     * Clear entire cart
     */
    @Transactional
    public void clearCart(Long customerId) {
        log.info("Clearing cart for customer: {}", customerId);
        cartRepository.deleteByCustomerId(customerId);
    }

    /**
     * Get cart item count
     */
    public int getCartItemCount(Long customerId) {
        return cartRepository.getCartItemCount(customerId);
    }

    /**
     * Check if cart exists
     */
    public boolean cartExists(Long customerId) {
        return cartRepository.existsByCustomerId(customerId);
    }

    /**
     * Convert CartItem entity to DTO
     */
    private CartItemDto convertToDto(CartItem cartItem) {
        return CartItemDto.builder()
                .customerId(Long.parseLong(cartItem.getCustomerId()))
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .imageUrl(cartItem.getImageUrl())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getTotalPrice())
                .addedAt(cartItem.getAddedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .build();
    }

    /**
     * Validate cart items against current product stock and prices
     * This should be called before checkout
     */
    public void validateCartItems(Long customerId) {
        log.info("Validating cart items for customer: {}", customerId);
        
        List<CartItem> cartItems = cartRepository.findByCustomerId(String.valueOf(customerId));
        
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));
            
            // Check stock availability
            if (product.getStockQty() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + 
                        ". Available: " + product.getStockQty() + ", Required: " + cartItem.getQuantity());
            }
            
            // Update price if it has changed
            if (!product.getPrice().equals(cartItem.getUnitPrice())) {
                cartItem.setUnitPrice(product.getPrice());
                cartItem.setProductName(product.getName());
                cartItem.setImageUrl(product.getImageUrl());
                cartRepository.save(cartItem);
                log.info("Updated price for product {} in cart", product.getName());
            }
        }
    }
}