package com.training.warehouse.enumeric;

public enum ProductType {
    AIRCON("Aircon"),
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
        throw new RuntimeException("Invalid product type: " + name);
    }
}
