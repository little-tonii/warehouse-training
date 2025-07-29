package com.training.warehouse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class InboundSummaryPerMonth {
    private int month;
    private String productType;
    private String supplierCd;
    private int totalQuantity;
}