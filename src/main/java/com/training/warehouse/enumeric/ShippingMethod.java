package com.training.warehouse.enumeric;

public enum ShippingMethod {
    AIR("A"),
    SEA("S"),
    TRAIL("T"),
    ROAD("R");

    private final String code;

    ShippingMethod(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ShippingMethod fromCode(String code) {
        for (ShippingMethod method : ShippingMethod.values()) {
            if (method.code.equals(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Invalid shipping method code: " + code);
    }
}
