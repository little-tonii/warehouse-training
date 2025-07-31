package com.training.warehouse.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOutboundRequest {
    @JsonProperty("inbound_id")
    @Min(1)
    private long inboundId;

    @JsonProperty("quantity")
    @Min(1)
    private long quantity;

    @JsonProperty("shipping_method")
    @Pattern(regexp = "[ASTR]", message = "shipping method must be one of A, S, T, R")
    private String shippingMethod;

    @JsonProperty("expected_shipping_date")
    @NotNull(message = "expected shipping date must not be null")
    @FutureOrPresent()
    private LocalDateTime exceptedShippingDate;
}
