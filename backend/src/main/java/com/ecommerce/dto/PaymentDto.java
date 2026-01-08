package com.ecommerce.dto;

import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDto {

    private Long paymentId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    private PaymentMethod paymentMethod;

    private PaymentStatus status;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;

    private String transactionRef;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Response DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private Long paymentId;
        private Long orderId;
        private PaymentMethod paymentMethod;
        private PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private String transactionRef;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String customerName;
        private String customerEmail;
    }

    // Create request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Order ID is required")
        private Long orderId;

        private PaymentMethod paymentMethod = PaymentMethod.MOCK;

        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
        private BigDecimal amount;

        @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
        private String currency = "USD";

        private String transactionRef;
    }

    // Update status request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull(message = "Status is required")
        private PaymentStatus status;

        private String transactionRef;
    }

    // Payment search request DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchRequest {
        private Long customerId;
        private PaymentStatus status;
        private PaymentMethod paymentMethod;
        private String currency;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private String sortBy; // "createdAt", "amount", "status"
        private String sortDirection; // "asc", "desc"
    }

    // Payment statistics DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long totalPayments;
        private BigDecimal totalAmount;
        private BigDecimal averageAmount;
        private Long successfulPayments;
        private Long failedPayments;
        private Long pendingPayments;
        private Double successRate;
    }

    // Payment summary DTO
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long paymentId;
        private Long orderId;
        private String customerName;
        private PaymentMethod paymentMethod;
        private PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private LocalDateTime createdAt;
    }
}