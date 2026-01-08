package com.ecommerce.entity;

import com.ecommerce.enums.PaymentMethod;
import com.ecommerce.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payments_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @NotNull(message = "Order is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_payments_order"))
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.MOCK;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PAID;

    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Size(max = 100, message = "Transaction reference must not exceed 100 characters")
    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public boolean isSuccessful() {
        return status == PaymentStatus.PAID;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public void markAsPaid(String transactionReference) {
        this.status = PaymentStatus.PAID;
        this.transactionRef = transactionReference;
    }

    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markAsPending() {
        this.status = PaymentStatus.PENDING;
    }
}