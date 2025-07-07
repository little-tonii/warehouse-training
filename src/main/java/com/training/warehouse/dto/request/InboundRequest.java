package com.training.warehouse.dto.request;

import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InboundRequest {
        private Long id;
        private String invoice;
        private ProductType productType;
        private SupplierCd supplierCd;
        private LocalDateTime receiveDate;
        private OrderStatus status;
        private long quantity;
}
