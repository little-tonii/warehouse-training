package com.training.warehouse.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOutboundByIdRequest {
    @JsonProperty("shipping_method")
    @Pattern(regexp = "[ASTR]", message = "Shipping method must be one of A, S, T, R")
    private String shippingMethod;

    @NotNull()
    private LocalDateTime expectedShippingDate;

    @JsonProperty("quantity")
    @Min(1)
    private long quantity;
}
