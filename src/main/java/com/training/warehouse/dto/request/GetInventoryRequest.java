package com.training.warehouse.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GetInventoryRequest {
    @NotNull(message = "page must not be null")
    @Min(1)
    private long page;

    @NotNull(message = "size must not be null")
    @Min(20)
    @Max(50)
    private long limit;
}
