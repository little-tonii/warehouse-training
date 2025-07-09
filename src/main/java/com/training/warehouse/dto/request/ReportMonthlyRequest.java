package com.training.warehouse.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportMonthlyRequest {

    @Parameter(required = true, description = "year for the report", name = "year")
    @NotNull(message = "year is required")
    @Min(value = 1900, message = "year must be greater than or equal to 1900")
    private int year;

    @Parameter(required = true, description = "month for the report", name = "month")
    @NotNull(message = "month is required")
    @Min(value = 1, message = "month must be between 1 and 12")
    @Max(value = 12, message = "month must be between 1 and 12")
    private int month;

    @Parameter(required = true, description = "page number for pagination", name = "page")
    @NotNull(message = "page is required")
    @Min(value = 1, message = "page must be greater than or equal to 1")
    private int page;

    @Parameter(required = true, description = "max number of items per page", name = "limit")
    @NotNull(message = "limit is required")
    @Min(value = 1, message = "limit must be greater than or equal to 1")
    @Max(value = 20, message = "limit must be less than or equal to 20")
    private int limit;
}
