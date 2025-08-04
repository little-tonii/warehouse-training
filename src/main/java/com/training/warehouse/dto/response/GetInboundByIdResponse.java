package com.training.warehouse.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetInboundByIdResponse {

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InboundCreatorResponse {
    
        @JsonProperty("email")
        private String email;

        @JsonProperty("full_name")
        private String fullName;
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InboundAttachmentResponse {

        @JsonProperty("id")
        private long id;
        
        @JsonProperty("file_name")
        private String fileName;
    }

    @JsonProperty("id")
    private long id;

    @JsonProperty("invoice")
    private String invoice;

    @JsonProperty("product_type")
    private String productType;

    @JsonProperty("supplier_cd")
    private String supplierCd;

    @JsonProperty("receive_date")
    private LocalDateTime receiveDate;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("quantity")
    private long quantity;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("creator")
    private InboundCreatorResponse creator;

    private List<InboundAttachmentResponse> attachments;
}
