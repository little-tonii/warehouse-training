package com.training.warehouse.dto.response;

import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public record InboundSummaryResponse(
        ProductType productType,
        SupplierCd supplierCd,
        Long total
        ) {
}
