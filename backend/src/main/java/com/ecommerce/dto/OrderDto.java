package com.ecommerce.dto;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderDto {

    private Long orderId;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private OrderStatus status;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    private String countryCode;

    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    private String invoiceS3Path;

    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;

    // Relationships
    private CustomerDto.Response customer;
    private List<OrderItemDto.Response> orderItems;
    private PaymentDto.Response payment;
    private ShippingAddressDto.Response shippingAddress;

    // Response DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private Long orderId;
        private Long customerId;
        private String customerName;
        private String customerEmail;
        private OrderStatus status;
        private String countryCode;
        private BigDecimal totalAmount;
        private String invoiceS3Path;
        private PaymentStatus paymentStatus;
        private LocalDateTime createdAt;
        private Integer totalItems;
        private List<OrderItemDto.Response> orderItems;
        private PaymentDto.Response payment;
        private ShippingAddressDto.Response shippingAddress;
    }

    // Create request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotBlank(message = "Country code is required")
        @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
        private String countryCode;

        @NotEmpty(message = "Order items are required")
        @Valid
        private List<OrderItemDto.CreateRequest> orderItems;
    }

    // Update status request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull(message = "Status is required")
        private OrderStatus status;
    }

    // Update payment status request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePaymentStatusRequest {
        @NotNull(message = "Payment status is required")
        private PaymentStatus paymentStatus;
    }

    // Order search request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private Long customerId;
        private OrderStatus status;
        private PaymentStatus paymentStatus;
        private String countryCode;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private String sortBy; // "createdAt", "totalAmount", "status"
        private String sortDirection; // "asc", "desc"
    }

    // Order summary DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long orderId;
        private String customerName;
        private OrderStatus status;
        private PaymentStatus paymentStatus;
        private BigDecimal totalAmount;
        private Integer totalItems;
        private LocalDateTime createdAt;
    }

    // Order statistics DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long totalOrders;
        private BigDecimal totalRevenue;
        private BigDecimal averageOrderValue;
        private Long pendingOrders;
        private Long completedOrders;
        private Long cancelledOrders;
    }

    // Checkout request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutRequest {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotBlank(message = "Country code is required")
        @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
        private String countryCode;

        @NotEmpty(message = "Order items are required")
        @Valid
        private List<OrderItemDto.CreateRequest> orderItems;

        @NotNull(message = "Payment method is required")
        private com.ecommerce.enums.PaymentMethod paymentMethod;

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
        private String currency;

        @NotNull(message = "Shipping address ID is required")
        private Long shippingAddressId;
    }

    // Checkout response DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutResponse {
        private OrderDto.Response order;
        private PaymentDto.Response payment;
        private String message;
    }
}