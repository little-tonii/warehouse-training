package com.training.warehouse.dto.response;

public interface InboundSummaryMonthProjection {
    Integer getMonth();
    String getProductType();
    String getSupplierCd();
    Integer getTotalQuantity();
}
