package com.training.warehouse.dto.response;

public interface StockProjection {
    Number startQuantity();
    Number endQuantity();
    Number diffQuantity();
}
