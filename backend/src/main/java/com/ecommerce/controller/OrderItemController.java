package com.ecommerce.controller;

import com.ecommerce.dto.OrderItemDto;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Item Management", description = "APIs for managing order items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Operation(summary = "Get order item by ID", description = "Retrieves an order item by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order item found"),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    @GetMapping("/{orderItemId}")
    public ResponseEntity<OrderItemDto.Response> getOrderItemById(
            @Parameter(description = "Order item ID") @PathVariable Long orderItemId) {
        log.info("Fetching order item with ID: {}", orderItemId);
        OrderItemDto.Response response = orderItemService.getOrderItemById(orderItemId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order items by order ID", description = "Retrieves all order items for a specific order")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemDto.Response>> getOrderItemsByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        log.info("Fetching order items for order ID: {}", orderId);
        List<OrderItemDto.Response> response = orderItemService.getOrderItemsByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order items by order ID with pagination", description = "Retrieves order items for a specific order with pagination")
    @GetMapping("/order/{orderId}/paginated")
    public ResponseEntity<Page<OrderItemDto.Response>> getOrderItemsByOrderIdPaginated(
            @Parameter(description = "Order ID") @PathVariable Long orderId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "orderItemId") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("Fetching order items for order ID: {} with pagination", orderId);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderItemDto.Response> response = orderItemService.getOrderItemsByOrderId(orderId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order items by product ID", description = "Retrieves all order items for a specific product")
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<OrderItemDto.Response>> getOrderItemsByProductId(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching order items for product ID: {}", productId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderItemId").descending());
        Page<OrderItemDto.Response> response = orderItemService.getOrderItemsByProductId(productId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order items by customer ID", description = "Retrieves all order items for a specific customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<OrderItemDto.Response>> getOrderItemsByCustomerId(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching order items for customer ID: {}", customerId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderItemId").descending());
        Page<OrderItemDto.Response> response = orderItemService.getOrderItemsByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search order items by product name", description = "Search order items by product name")
    @GetMapping("/search/product")
    public ResponseEntity<Page<OrderItemDto.Response>> getOrderItemsByProductName(
            @Parameter(description = "Product name") @RequestParam String productName,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching order items by product name: {}", productName);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderItemId").descending());
        Page<OrderItemDto.Response> response = orderItemService.getOrderItemsByProductName(productName, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order items by order status", description = "Retrieves order items for orders with specific status")
    @GetMapping("/order-status/{status}")
    public ResponseEntity<Page<OrderItemDto.Response>> getOrderItemsByOrderStatus(
            @Parameter(description = "Order status") @PathVariable OrderStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching order items for order status: {}", status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderItemId").descending());
        Page<OrderItemDto.Response> response = orderItemService.getOrderItemsByOrderStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order items by quantity", description = "Retrieves order items with quantity greater than specified value")
    @GetMapping("/quantity-greater-than/{quantity}")
    public ResponseEntity<Page<OrderItemDto.Response>> getOrderItemsByQuantityGreaterThan(
            @Parameter(description = "Minimum quantity") @PathVariable Integer quantity,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching order items with quantity greater than: {}", quantity);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("quantity").descending());
        Page<OrderItemDto.Response> response = orderItemService.getOrderItemsByQuantityGreaterThan(quantity, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order items by unit price range", description = "Retrieves order items within a unit price range")
    @GetMapping("/price-range")
    public ResponseEntity<Page<OrderItemDto.Response>> getOrderItemsByUnitPriceRange(
            @Parameter(description = "Minimum price") @RequestParam BigDecimal minPrice,
            @Parameter(description = "Maximum price") @RequestParam BigDecimal maxPrice,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching order items with unit price range: {} - {}", minPrice, maxPrice);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("unitPrice").ascending());
        Page<OrderItemDto.Response> response = orderItemService.getOrderItemsByUnitPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all order items", description = "Retrieves all order items with pagination")
    @GetMapping
    public ResponseEntity<Page<OrderItemDto.Response>> getAllOrderItems(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "orderItemId") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Fetching all order items - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderItemDto.Response> response = orderItemService.getAllOrderItems(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update order item", description = "Updates order item quantity and/or unit price")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    @PutMapping("/{orderItemId}")
    public ResponseEntity<OrderItemDto.Response> updateOrderItem(
            @Parameter(description = "Order item ID") @PathVariable Long orderItemId,
            @Valid @RequestBody OrderItemDto.UpdateRequest request) {
        log.info("Updating order item with ID: {}", orderItemId);
        OrderItemDto.Response response = orderItemService.updateOrderItemQuantity(orderItemId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete order item", description = "Deletes an order item and restores stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order item deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Order item cannot be deleted"),
            @ApiResponse(responseCode = "404", description = "Order item not found")
    })
    @DeleteMapping("/{orderItemId}")
    public ResponseEntity<Void> deleteOrderItem(
            @Parameter(description = "Order item ID") @PathVariable Long orderItemId) {
        log.info("Deleting order item with ID: {}", orderItemId);
        orderItemService.deleteOrderItem(orderItemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get total quantity sold for product", description = "Returns total quantity sold for a specific product")
    @GetMapping("/product/{productId}/total-quantity")
    public ResponseEntity<Long> getTotalQuantitySoldForProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        log.info("Fetching total quantity sold for product ID: {}", productId);
        Long totalQuantity = orderItemService.getTotalQuantitySoldForProduct(productId);
        return ResponseEntity.ok(totalQuantity);
    }

    @Operation(summary = "Get total revenue for product", description = "Returns total revenue for a specific product")
    @GetMapping("/product/{productId}/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueForProduct(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        log.info("Fetching total revenue for product ID: {}", productId);
        BigDecimal totalRevenue = orderItemService.getTotalRevenueForProduct(productId);
        return ResponseEntity.ok(totalRevenue);
    }

    @Operation(summary = "Get top selling products by quantity", description = "Returns top selling products by quantity sold")
    @GetMapping("/analytics/top-selling-by-quantity")
    public ResponseEntity<Page<Object[]>> getTopSellingProductsByQuantity(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching top selling products by quantity");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> response = orderItemService.getTopSellingProductsByQuantity(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get top selling products by revenue", description = "Returns top selling products by revenue generated")
    @GetMapping("/analytics/top-selling-by-revenue")
    public ResponseEntity<Page<Object[]>> getTopSellingProductsByRevenue(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching top selling products by revenue");
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> response = orderItemService.getTopSellingProductsByRevenue(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Calculate order total", description = "Calculates total amount for an order based on its items")
    @GetMapping("/order/{orderId}/total")
    public ResponseEntity<BigDecimal> calculateOrderTotal(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        log.info("Calculating total for order ID: {}", orderId);
        BigDecimal orderTotal = orderItemService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(orderTotal);
    }

    @Operation(summary = "Get product sales statistics", description = "Returns detailed sales statistics for a product")
    @GetMapping("/product/{productId}/statistics")
    public ResponseEntity<Object> getProductSalesStatistics(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        log.info("Fetching sales statistics for product ID: {}", productId);
        Object statistics = orderItemService.getProductSalesStatistics(productId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Count order items by order", description = "Returns the number of items in an order")
    @GetMapping("/order/{orderId}/count")
    public ResponseEntity<Long> countOrderItemsByOrder(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        log.info("Counting order items for order ID: {}", orderId);
        long itemCount = orderItemService.countOrderItemsByOrder(orderId);
        return ResponseEntity.ok(itemCount);
    }
}