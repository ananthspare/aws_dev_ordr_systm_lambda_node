package com.ecommerce.service;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.OrderItemDto;
import com.ecommerce.dto.PaymentDto;
import com.ecommerce.entity.Customer;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final PaymentService paymentService;

    /**
     * Checkout - Create order and process payment in single transaction
     */
    public OrderDto.CheckoutResponse checkout(OrderDto.CheckoutRequest request) {
        String traceId = MDC.get("traceId");
        log.info("[{}] SERVICE: Starting checkout for customer ID: {} with {} items", 
                traceId, request.getCustomerId(), request.getOrderItems().size());

        try {
            // Create order
            OrderDto.CreateRequest orderRequest = OrderDto.CreateRequest.builder()
                    .customerId(request.getCustomerId())
                    .countryCode(request.getCountryCode())
                    .orderItems(request.getOrderItems())
                    .build();

            OrderDto.Response order = createOrder(orderRequest);
            log.info("[{}] SERVICE: Order created successfully - ID: {}, Total: ${}", 
                    traceId, order.getOrderId(), order.getTotalAmount());

            // Create payment
            PaymentDto.CreateRequest paymentRequest = PaymentDto.CreateRequest.builder()
                    .orderId(order.getOrderId())
                    .paymentMethod(request.getPaymentMethod())
                    .amount(order.getTotalAmount())
                    .currency(request.getCurrency())
                    .build();

            PaymentDto.Response payment = paymentService.createPayment(paymentRequest);
            log.info("[{}] SERVICE: Payment created successfully - ID: {}, Status: {}", 
                    traceId, payment.getPaymentId(), payment.getStatus());

            // Update order payment status
            updatePaymentStatus(order.getOrderId(), 
                    OrderDto.UpdatePaymentStatusRequest.builder()
                            .paymentStatus(payment.getStatus())
                            .build());

            String message = payment.getStatus() == PaymentStatus.PAID ? 
                    "Order placed and payment processed successfully" : 
                    "Order placed, payment is pending";

            log.info("[{}] SERVICE: Checkout completed successfully - Order: {}, Payment: {}", 
                    traceId, order.getOrderId(), payment.getPaymentId());

            return OrderDto.CheckoutResponse.builder()
                    .order(order)
                    .payment(payment)
                    .message(message)
                    .build();

        } catch (Exception e) {
            log.error("[{}] SERVICE: Checkout failed for customer ID: {} - Error: {}", 
                    traceId, request.getCustomerId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Create a new order
     */
    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
        String traceId = MDC.get("traceId");
        log.info("[{}] SERVICE: Creating new order for customer ID: {} with {} items", 
                traceId, request.getCustomerId(), request.getOrderItems().size());
        log.debug("[{}] SERVICE: Order request details - Country: {}, Items: {}", 
                traceId, request.getCountryCode(), request.getOrderItems());

        try {
            // Validate customer exists
            log.debug("[{}] SERVICE: Validating customer ID: {}", traceId, request.getCustomerId());
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> {
                        log.error("[{}] SERVICE: Customer not found with ID: {}", traceId, request.getCustomerId());
                        return new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId());
                    });
            log.debug("[{}] SERVICE: Customer validated - Name: {}, Email: {}", 
                    traceId, customer.getName(), customer.getEmail());

            // Create order
            log.debug("[{}] SERVICE: Creating order entity", traceId);
            Order order = Order.builder()
                    .customer(customer)
                    .status(OrderStatus.CREATED)
                    .countryCode(request.getCountryCode())
                    .paymentStatus(PaymentStatus.PENDING)
                    .orderItems(new ArrayList<>())
                    .build();

            // Process order items
            BigDecimal totalAmount = BigDecimal.ZERO;
            log.debug("[{}] SERVICE: Processing {} order items", traceId, request.getOrderItems().size());
            
            for (int i = 0; i < request.getOrderItems().size(); i++) {
                OrderItemDto.CreateRequest itemRequest = request.getOrderItems().get(i);
                log.debug("[{}] SERVICE: Processing item {}/{} - Product ID: {}, Quantity: {}", 
                        traceId, i + 1, request.getOrderItems().size(), 
                        itemRequest.getProductId(), itemRequest.getQuantity());
                
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> {
                            log.error("[{}] SERVICE: Product not found with ID: {}", traceId, itemRequest.getProductId());
                            return new ResourceNotFoundException("Product not found with ID: " + itemRequest.getProductId());
                        });
                
                log.debug("[{}] SERVICE: Product found - Name: {}, Price: ${}, Stock: {}", 
                        traceId, product.getName(), product.getPrice(), product.getStockQty());

                // Check stock availability
                if (!product.hasStock(itemRequest.getQuantity())) {
                    log.error("[{}] SERVICE: Insufficient stock for product {} - Available: {}, Requested: {}", 
                            traceId, product.getName(), product.getStockQty(), itemRequest.getQuantity());
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() + 
                            ". Available: " + product.getStockQty() + ", Requested: " + itemRequest.getQuantity());
                }

                // Create order item
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(product.getPrice())
                        .build();

                order.addOrderItem(orderItem);
                BigDecimal itemTotal = orderItem.getTotalPrice();
                totalAmount = totalAmount.add(itemTotal);
                
                log.debug("[{}] SERVICE: Order item created - Total: ${}, Running total: ${}", 
                        traceId, itemTotal, totalAmount);

                // Reduce product stock
                log.debug("[{}] SERVICE: Reducing stock for product {} by {}", 
                        traceId, product.getName(), itemRequest.getQuantity());
                productService.reduceStock(product.getProductId(), itemRequest.getQuantity());
            }

            order.setTotalAmount(totalAmount);
            log.debug("[{}] SERVICE: Order total calculated: ${}", traceId, totalAmount);

            log.debug("[{}] SERVICE: Saving order to database", traceId);
            Order savedOrder = orderRepository.save(order);
            
            log.info("[{}] SERVICE: Order created successfully - ID: {}, Total: ${}, Items: {}", 
                    traceId, savedOrder.getOrderId(), savedOrder.getTotalAmount(), 
                    savedOrder.getOrderItems().size());

            return mapToResponse(savedOrder);
        } catch (Exception e) {
            log.error("[{}] SERVICE: Failed to create order for customer ID: {} - Error: {}", 
                    traceId, request.getCustomerId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDto.Response getOrderById(Long orderId) {
        String traceId = MDC.get("traceId");
        log.debug("[{}] SERVICE: Fetching order with ID: {}", traceId, orderId);

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("[{}] SERVICE: Order not found with ID: {}", traceId, orderId);
                        return new ResourceNotFoundException("Order not found with ID: " + orderId);
                    });
            
            log.debug("[{}] SERVICE: Order found - Status: {}, Customer: {}, Items: {}", 
                    traceId, order.getStatus(), order.getCustomer().getName(), 
                    order.getOrderItems().size());

            return mapToResponse(order);
        } catch (Exception e) {
            log.error("[{}] SERVICE: Failed to fetch order ID: {} - Error: {}", 
                    traceId, orderId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get all orders with pagination
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with pagination: {}", pageable);

        return orderRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get orders by customer ID
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Fetching orders for customer ID: {}", customerId);

        return orderRepository.findByCustomerCustomerId(customerId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders with status: {}", status);

        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get orders by payment status
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrdersByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable) {
        log.debug("Fetching orders with payment status: {}", paymentStatus);

        return orderRepository.findByPaymentStatus(paymentStatus, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get orders by country code
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrdersByCountryCode(String countryCode, Pageable pageable) {
        log.debug("Fetching orders for country: {}", countryCode);

        return orderRepository.findByCountryCode(countryCode, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get orders by date range
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Fetching orders between {} and {}", startDate, endDate);

        return orderRepository.findByCreatedAtBetween(startDate, endDate, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Update order status
     */
    public OrderDto.Response updateOrderStatus(Long orderId, OrderDto.UpdateStatusRequest request) {
        log.info("Updating order status for ID: {} to {}", orderId, request.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Validate status transition
        if (!order.getStatus().canTransitionTo(request.getStatus())) {
            throw new IllegalArgumentException("Cannot transition from " + order.getStatus() + " to " + request.getStatus());
        }

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully for ID: {}", updatedOrder.getOrderId());
        return mapToResponse(updatedOrder);
    }

    /**
     * Update payment status
     */
    public OrderDto.Response updatePaymentStatus(Long orderId, OrderDto.UpdatePaymentStatusRequest request) {
        log.info("Updating payment status for order ID: {} to {}", orderId, request.getPaymentStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setPaymentStatus(request.getPaymentStatus());
        Order updatedOrder = orderRepository.save(order);

        log.info("Payment status updated successfully for order ID: {}", updatedOrder.getOrderId());
        return mapToResponse(updatedOrder);
    }

    /**
     * Update invoice S3 path
     */
    public OrderDto.Response updateInvoiceS3Path(Long orderId, String s3Path) {
        log.info("Updating invoice S3 path for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setInvoiceS3Path(s3Path);
        Order updatedOrder = orderRepository.save(order);

        log.info("Invoice S3 path updated successfully for order ID: {}", updatedOrder.getOrderId());
        return mapToResponse(updatedOrder);
    }

    /**
     * Cancel order
     */
    public OrderDto.Response cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (!order.canBeCancelled()) {
            throw new IllegalArgumentException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        // Restore stock for cancelled order
        for (OrderItem item : order.getOrderItems()) {
            productService.increaseStock(item.getProduct().getProductId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled successfully with ID: {}", cancelledOrder.getOrderId());
        return mapToResponse(cancelledOrder);
    }

    /**
     * Search orders by customer name or email
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> searchOrdersByCustomer(String searchTerm, Pageable pageable) {
        log.debug("Searching orders by customer with term: {}", searchTerm);

        return orderRepository.searchOrdersByCustomer(searchTerm, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get orders with invoices
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrdersWithInvoice(Pageable pageable) {
        log.debug("Fetching orders with invoices");

        return orderRepository.findOrdersWithInvoice(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get orders without invoices
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.Response> getOrdersWithoutInvoice(Pageable pageable) {
        log.debug("Fetching orders without invoices");

        return orderRepository.findOrdersWithoutInvoice(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get recent orders for customer
     */
    @Transactional(readOnly = true)
    public List<OrderDto.Response> getRecentOrdersByCustomer(Long customerId, int limit) {
        log.debug("Fetching recent orders for customer ID: {}", customerId);

        Pageable pageable = Pageable.ofSize(limit);
        return orderRepository.findRecentOrdersByCustomer(customerId, pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Calculate total revenue
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return orderRepository.calculateTotalRevenue();
    }

    /**
     * Calculate revenue by date range
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.calculateRevenueByDateRange(startDate, endDate);
    }

    /**
     * Calculate revenue by country
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueByCountry(String countryCode) {
        return orderRepository.calculateRevenueByCountry(countryCode);
    }

    /**
     * Get order statistics
     */
    @Transactional(readOnly = true)
    public OrderDto.Statistics getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Object stats = orderRepository.getOrderStatistics(startDate, endDate).orElse(null);
        
        if (stats == null) {
            return OrderDto.Statistics.builder()
                    .totalOrders(0L)
                    .totalRevenue(BigDecimal.ZERO)
                    .averageOrderValue(BigDecimal.ZERO)
                    .build();
        }

        // Note: This would need proper mapping based on the actual return type
        // For now, returning basic statistics
        return OrderDto.Statistics.builder()
                .totalOrders(orderRepository.count())
                .totalRevenue(calculateTotalRevenue())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.CREATED))
                .completedOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .cancelledOrders(orderRepository.countByStatus(OrderStatus.CANCELLED))
                .build();
    }

    /**
     * Map Order entity to Response DTO
     */
    private OrderDto.Response mapToResponse(Order order) {
        return OrderDto.Response.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomer().getCustomerId())
                .customerName(order.getCustomer().getName())
                .customerEmail(order.getCustomer().getEmail())
                .status(order.getStatus())
                .countryCode(order.getCountryCode())
                .totalAmount(order.getTotalAmount())
                .invoiceS3Path(order.getInvoiceS3Path())
                .paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt())
                .totalItems(order.getTotalItems())
                .orderItems(order.getOrderItems().stream()
                        .map(this::mapOrderItemToResponse)
                        .toList())
                .build();
    }

    /**
     * Map OrderItem entity to Response DTO
     */
    private OrderItemDto.Response mapOrderItemToResponse(OrderItem orderItem) {
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