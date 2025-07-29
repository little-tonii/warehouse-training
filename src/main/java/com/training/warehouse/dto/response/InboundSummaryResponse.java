package com.training.warehouse.dto.response;

import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;

public record InboundSummaryResponse(
        ProductType productType,
        SupplierCd supplierCd,
        Long total
) {
}
