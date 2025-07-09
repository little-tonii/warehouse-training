package com.training.warehouse.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportMonthlyResponse {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReportMonthlyData {

        @JsonProperty("inbound_id")
        private long inbound_id;

        @JsonProperty("invoice")
        private String invoice;

        @JsonProperty("product_type")
        private String productType;

        @JsonProperty("supplier_cd")
        private String supplierCd;

        @JsonProperty("receive_date")
        private LocalDateTime receiveDate;

        @JsonProperty("status")
        private long status;

        @JsonProperty("quantity")
        private long quantity;

        @JsonProperty("begin_month_quantity")
        private long beginMonthQuantity;

        @JsonProperty("end_month_quantity")
        private long endMonthQuantity;

        @JsonProperty("difference_month_quantity")
        private long differenceMonthQuantity;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;
    }

    @JsonProperty("year")
    private long year;

    @JsonProperty("month")
    private long month;
    
    @JsonProperty("page")
    private long page;

    @JsonProperty("limit")
    private long limit;

    @JsonProperty("total")
    private long total;

    @JsonProperty("monthly_data")
    private List<ReportMonthlyData> monthlyData;

}
