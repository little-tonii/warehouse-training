package com.training.warehouse.repository.projection;

import java.time.LocalDateTime;

public interface InboundProjection {
    long getId();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    String getInvoice();
    String getProductType();
    String getSupplierCd();
    LocalDateTime getReceiveDate();
    long getOrderStatus();
    long getQuantity();
    String getCreatorFullName();
    String getCreatorEmail();
}
