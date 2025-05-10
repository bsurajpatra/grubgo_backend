package klu.model;

public enum OrderStatus {
    PLACED("Order Placed"),
    PENDING("Pending"),
    PREPARING("Preparing"),
    READY("Ready for Pickup"),
    PROCESSING("Processing"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canBeCancelled() {
        return this == PLACED || this == PENDING || this == PREPARING || this == PROCESSING;
    }
} 