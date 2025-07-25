package com.training.warehouse.enumeric;

public enum SupplierCd {
    VIET_NAM("VN"),
    THAILAND("TH"),
    MALAYSIA("MY"),
    INDONESIA("ID"),
    SINGAPORE("SG"),
    PHILIPPINES("PH"),
    LAOS("LA"),
    MYANMAR("MM"),
    DONGTIMOR("TL");

    private final String code;

    SupplierCd(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static SupplierCd fromCode(String code) {
        for (SupplierCd supplier : SupplierCd.values()) {
            if (supplier.code.equals(code)) {
                return supplier;
            }
        }
        throw new RuntimeException("Invalid supplier code: " + code);
    }
}
