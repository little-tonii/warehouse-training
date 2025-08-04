package com.training.warehouse.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetInboundsRequest {

    @NotNull(message = "page must not be null")
    @Min(1)
    private long page;

    @NotNull(message = "size must not be null")
    @Min(20)
    @Max(50)
    private long limit;

    @NotNull(message = "direction must not be null")
    @Pattern(regexp = "asc|desc", message = "direction must be either 'asc' or 'desc'")
    private String direction;
}
