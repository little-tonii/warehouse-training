package com.training.warehouse.dto.response;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Data
public class InboundResponse {
    private Long id;
    private String invoice;
    private ProductType productType;
    private SupplierCd supplierCd;
    private LocalDateTime receiveDate;
    private OrderStatus status;
    private long quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<InboundAttachmentEntity> inboundAttachments;
    private List<FileUploadResult> results;
}
