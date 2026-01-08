package com.ecommerce.enums;

public enum PaymentStatus {
    PENDING("Payment Pending"),
    PAID("Payment Completed"),
    FAILED("Payment Failed"),
    REFUNDED("Payment Refunded");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCompleted() {
        return this == PAID;
    }

    public boolean canRefund() {
        return this == PAID;
    }
}