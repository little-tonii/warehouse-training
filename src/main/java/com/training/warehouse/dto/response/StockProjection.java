package com.training.warehouse.dto.response;

public interface StockProjection {
    Integer getStartQuantity();
    Integer getEndQuantity();
    Integer getDiffQuantity();
}
