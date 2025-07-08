package com.training.warehouse.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 9,min = 9)
    @Pattern(regexp = "\\d{9}", message = "Invoice must be exactly 9 digits")
    private String invoice;

    @NotNull
    private ProductType productType;

    @NotNull
    private SupplierCd supplierCd;

    private LocalDateTime receiveDate;

    private OrderStatus status;

    @NotNull
    @Min(value = 1)
    private long quantity;
}
