package com.ecommerce.enums;

public enum PaymentMethod {
    MOCK("Mock Payment"),
    CARD("Credit/Debit Card"),
    UPI("UPI Payment"),
    PAYPAL("PayPal"),
    BANK_TRANSFER("Bank Transfer"),
    CASH_ON_DELIVERY("Cash on Delivery");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresOnlineProcessing() {
        return this != CASH_ON_DELIVERY && this != MOCK;
    }
}