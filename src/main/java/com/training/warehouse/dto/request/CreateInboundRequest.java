package com.training.warehouse.dto.request;

import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.training.warehouse.service.FileStoreService.ALLOWED_CONTENT_TYPES;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class CreateInboundRequest {
    @Size(max = 9, min = 9)
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

    private List<MultipartFile> attachments;
}