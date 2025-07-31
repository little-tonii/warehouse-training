package com.training.warehouse.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInboundByIdRequest {
    @Size(max = 9, min = 9)
    @NotNull(message = "invoice must not be null")
    @Pattern(regexp = "\\d{9}", message = "invoice must be exactly 9 digits")
    private String invoice;

    @NotNull(message = "product type must not be null")
    @Pattern(regexp = "Aircon|Spare_part", message = "invalid product type")
    private String productType;

    @NotNull(message = "supply code must not be null")
    @Pattern(
        regexp = "VN|TH|MY|ID|SG|PH|LA|MM|TL",
        message = "invalid supplier code"
    )
    private String supplierCd;

    @NotNull(message = "receive date must not be null")
    @FutureOrPresent
    private LocalDateTime receiveDate;

    @NotNull(message = "order status must not be null")
    @Min(0)
    @Max(2)
    private long orderStatus;

    @Min(value = 1)
    @NotNull(message = "quantity must not be null")
    private long quantity;

    @Size(min = 0, max = 5, message = "Number of attachments must be between 1 and 5")
    private List<MultipartFile> attachments;
}
