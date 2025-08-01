package com.training.warehouse.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetInventoryResponse {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class InventoryProjectionRepsonse {

        @JsonProperty("id")
        private long id;

        @JsonProperty("created_at")
        private LocalDateTime createdAt;

        @JsonProperty("updated_at")
        private LocalDateTime updatedAt;

        @JsonProperty("invoice")
        private String invoice;

        @JsonProperty("product_type")
        private String productType;

        @JsonProperty("supplier_cd")
        private String SupplierCd;

        @JsonProperty("receive_date")
        private LocalDateTime receiveDate;

        @JsonProperty("quantity")
        private long quantity;

        @JsonProperty("inventory")
        private long inventory;
    }

    @JsonProperty("page")
    private long page;

    @JsonProperty("limit")
    private long limit;

    @JsonProperty("total")
    private long total;

    @JsonProperty("inventories")
    private List<InventoryProjectionRepsonse> inventories;
}
