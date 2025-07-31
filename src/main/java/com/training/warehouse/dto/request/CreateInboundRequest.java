package com.training.warehouse.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateInboundRequest {
    @Size(max = 9, min = 9)
    @Pattern(regexp = "\\d{9}", message = "Invoice must be exactly 9 digits")
    private String invoice;

    @Pattern(regexp = "Aircon|Spare_part", message = "Invalid product type")
    private String productType;

    @Pattern(
        regexp = "VN|TH|MY|ID|SG|PH|LA|MM|TL",
        message = "Invalid supplier code"
    )
    private String supplierCd;

    @NotNull
    @FutureOrPresent
    private LocalDateTime receiveDate;

    @Min(0)
    @Max(2)
    private long orderStatus;

    @Min(value = 1)
    private long quantity;

    @NotNull(message = "Attachments must not be null")
    @Size(min = 1, max = 5, message = "Number of attachments must be between 1 and 5")
    private List<MultipartFile> attachments;
}