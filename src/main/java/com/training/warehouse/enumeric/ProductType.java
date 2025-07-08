package com.training.warehouse.enumeric;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProductType {
    AIRCORN("Aircorn"),
    SPARE_PART("Spare_part");

    private final String name;

    ProductType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ProductType fromString(String name){
        for (ProductType type : ProductType.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid product type: " + name);
    }
}
