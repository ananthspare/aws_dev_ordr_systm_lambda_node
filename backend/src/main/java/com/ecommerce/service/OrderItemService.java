package com.ecommerce.service;

import com.ecommerce.dto.OrderItemDto;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * Get order item by ID
     */
    @Transactional(readOnly = true)
    public OrderItemDto.Response getOrderItemById(Long orderItemId) {
        log.debug("Fetching order item with ID: {}", orderItemId);

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with ID: " + orderItemId));

        return mapToResponse(orderItem);
    }

    /**
     * Get order items by order ID
     */
    @Transactional(readOnly = true)
    public List<OrderItemDto.Response> getOrderItemsByOrderId(Long orderId) {
        log.debug("Fetching order items for order ID: {}", orderId);

        return orderItemRepository.findByOrderOrderId(orderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get order items by order ID with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderItemDto.Response> getOrderItemsByOrderId(Long orderId, Pageable pageable) {
        log.debug("Fetching order items for order ID: {} with pagination", orderId);

        return orderItemRepository.findByOrderOrderId(orderId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get order items by product ID
     */
    @Transactional(readOnly = true)
    public Page<OrderItemDto.Response> getOrderItemsByProductId(Long productId, Pageable pageable) {
        log.debug("Fetching order items for product ID: {}", productId);

        return orderItemRepository.findByProductProductId(productId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get order items by customer ID
     */
    @Transactional(readOnly = true)
    public Page<OrderItemDto.Response> getOrderItemsByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Fetching order items for customer ID: {}", customerId);

        return orderItemRepository.findByCustomerId(customerId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get order items by product name
     */
    @Transactional(readOnly = true)
    public Page<OrderItemDto.Response> getOrderItemsByProductName(String productName, Pageable pageable) {
        log.debug("Fetching order items for product name containing: {}", productName);

        return orderItemRepository.findByProductNameContaining(productName, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get order items by order status
     */
    @Transactional(readOnly = true)
    public Page<OrderItemDto.Response> getOrderItemsByOrderStatus(com.ecommerce.enums.OrderStatus status, Pageable pageable) {
        log.debug("Fetching order items for order status: {}", status);

        return orderItemRepository.findByOrderStatus(status, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get order items with quantity greater than specified value
     */
    @Transactional(readOnly = true)
    public Page<OrderItemDto.Response> getOrderItemsByQuantityGreaterThan(Integer quantity, Pageable pageable) {
        log.debug("Fetching order items with quantity greater than: {}", quantity);

        return orderItemRepository.findByQuantityGreaterThan(quantity, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get order items by unit price range
     */
    @Transactional(readOnly = true)
    public Page<OrderItemDto.Response> getOrderItemsByUnitPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("Fetching order items with unit price range: {} - {}", minPrice, maxPrice);

        return orderItemRepository.findByUnitPriceBetween(minPrice, maxPrice, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Update order item quantity
     */
    public OrderItemDto.Response updateOrderItemQuantity(Long orderItemId, OrderItemDto.UpdateRequest request) {
        log.info("Updating order item quantity for ID: {}", orderItemId);

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with ID: " + orderItemId));

        // Check if order can be modified (only if order is in CREATED status)
        if (orderItem.getOrder().getStatus() != com.ecommerce.enums.OrderStatus.CREATED) {
            throw new IllegalArgumentException("Cannot modify order item. Order status: " + orderItem.getOrder().getStatus());
        }

        Integer oldQuantity = orderItem.getQuantity();
        Integer newQuantity = request.getQuantity();
        Integer quantityDifference = newQuantity - oldQuantity;

        // Check stock availability if increasing quantity
        if (quantityDifference > 0) {
            Product product = orderItem.getProduct();
            if (!product.hasStock(quantityDifference)) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + 
                        ". Available: " + product.getStockQty() + ", Additional needed: " + quantityDifference);
            }
            // Reduce additional stock
            product.reduceStock(quantityDifference);
        } else if (quantityDifference < 0) {
            // Restore stock if decreasing quantity
            Product product = orderItem.getProduct();
            product.increaseStock(Math.abs(quantityDifference));
        }

        // Update order item
        orderItem.setQuantity(newQuantity);
        if (request.getUnitPrice() != null) {
            orderItem.setUnitPrice(request.getUnitPrice());
        }

        // Update order total
        Order order = orderItem.getOrder();
        BigDecimal newTotal = order.calculateTotalAmount();
        order.setTotalAmount(newTotal);

        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
        orderRepository.save(order);

        log.info("Order item updated successfully with ID: {}", updatedOrderItem.getOrderItemId());
        return mapToResponse(updatedOrderItem);
    }

    /**
     * Delete order item
     */
    public void deleteOrderItem(Long orderItemId) {
        log.info("Deleting order item with ID: {}", orderItemId);

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with ID: " + orderItemId));

        // Check if order can be modified
        if (orderItem.getOrder().getStatus() != com.ecommerce.enums.OrderStatus.CREATED) {
            throw new IllegalArgumentException("Cannot delete order item. Order status: " + orderItem.getOrder().getStatus());
        }

        // Restore stock
        Product product = orderItem.getProduct();
        product.increaseStock(orderItem.getQuantity());
        productRepository.save(product);

        // Update order total
        Order order = orderItem.getOrder();
        order.removeOrderItem(orderItem);
        BigDecimal newTotal = order.calculateTotalAmount();
        order.setTotalAmount(newTotal);
        orderRepository.save(order);

        orderItemRepository.delete(orderItem);
        log.info("Order item deleted successfully with ID: {}", orderItemId);
    }

    /**
     * Get total quantity sold for a product
     */
    @Transactional(readOnly = true)
    public Long getTotalQuantitySoldForProduct(Long productId) {
        log.debug("Calculating total quantity sold for product ID: {}", productId);
        return orderItemRepository.getTotalQuantitySoldForProduct(productId);
    }

    /**
     * Get total revenue for a product
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueForProduct(Long productId) {
        log.debug("Calculating total revenue for product ID: {}", productId);
        return orderItemRepository.getTotalRevenueForProduct(productId);
    }

    /**
     * Get top selling products by quantity
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getTopSellingProductsByQuantity(Pageable pageable) {
        log.debug("Fetching top selling products by quantity");
        return orderItemRepository.findTopSellingProductsByQuantity(pageable);
    }

    /**
     * Get top selling products by revenue
     */
    @Transactional(readOnly = true)
    public Page<Object[]> getTopSellingProductsByRevenue(Pageable pageable) {
        log.debug("Fetching top selling products by revenue");
        return orderItemRepository.findTopSellingProductsByRevenue(pageable);
    }

    /**
     * Calculate order total
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateOrderTotal(Long orderId) {
        log.debug("Calculating total for order ID: {}", orderId);
        return orderItemRepository.calculateOrderTotal(orderId);
    }

    /**
     * Get product sales statistics
     */
    @Transactional(readOnly = true)
    public Object getProductSalesStatistics(Long productId) {
        log.debug("Fetching sales statistics for product ID: {}", productId);
        return orderItemRepository.getProductSalesStatistics(productId);
    }

    /**
     * Count order items by order
     */
    @Transactional(readOnly = true)
    public long countOrderItemsByOrder(Long orderId) {
        return orderItemRepository.countByOrderOrderId(orderId);
    }

    /**
     * Get all order items with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderItemDto.Response> getAllOrderItems(Pageable pageable) {
        log.debug("Fetching all order items with pagination: {}", pageable);

        return orderItemRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Map OrderItem entity to Response DTO
     */
    private OrderItemDto.Response mapToResponse(OrderItem orderItem) {
        return OrderItemDto.Response.builder()
                .orderItemId(orderItem.getOrderItemId())
                .orderId(orderItem.getOrder().getOrderId())
                .productId(orderItem.getProduct().getProductId())
                .productName(orderItem.getProduct().getName())
                .productDescription(orderItem.getProduct().getDescription())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}