package com.training.warehouse.repository.projection;

import java.time.LocalDateTime;

public interface InventoryProjection {
    long getId();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    String getInvoice();
    String getProductType();
    String getSupplierCd();
    LocalDateTime getReceiveDate();
    long getQuantity();
    long getInventory();
}
