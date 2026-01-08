package com.ecommerce.enums;

public enum OrderStatus {
    CREATED("Order Created"),
    CONFIRMED("Order Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return this != CANCELLED && this != REFUNDED;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case CREATED -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == PROCESSING || newStatus == CANCELLED;
            case PROCESSING -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED -> newStatus == REFUNDED;
            case CANCELLED, REFUNDED -> false;
        };
    }
}