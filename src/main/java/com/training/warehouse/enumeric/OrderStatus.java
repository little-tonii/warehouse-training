package com.training.warehouse.enumeric;

public enum OrderStatus {
    NOT_EXPORTED(0),      
    PARTIALLY_EXPORTED(1),
    FULLY_EXPORTED(2); 


    private final long value;

    OrderStatus(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public static OrderStatus fromValue(long value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new RuntimeException("Invalid OrderStatus value: " + value);
    }
}
