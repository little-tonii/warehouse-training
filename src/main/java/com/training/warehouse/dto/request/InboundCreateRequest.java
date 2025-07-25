package com.training.warehouse.dto.request;

import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

public class InboundCreateRequest {
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

    // Nếu cần
    public void validate() {
        if (attachments == null) attachments = Collections.emptyList();;
        if (attachments.size() > 5) {
            throw new BadRequestException(ExceptionMessage.QUANTITY_FILE_IS_INVALID);
        }

        Set<String> fileNames = new HashSet<>();
        for (MultipartFile file : attachments) {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isBlank()) {
                throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
            }
            String cleanedFileName = Paths.get(originalFileName).getFileName().toString();

            if (!fileNames.add(cleanedFileName)) {
                throw new BadRequestException("Duplicate file name: " + cleanedFileName);
            }

            if (!cleanedFileName.matches("^[a-zA-Z0-9._-]{1,255}$")) {
                throw new BadRequestException(ExceptionMessage.FILENAME_IS_NOT_VALID);
            }
            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                throw new BadRequestException(ExceptionMessage.FILETYPE_NOT_ALLOWED);
            }
        }
    }
}
