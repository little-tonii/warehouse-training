package com.training.warehouse.service;

import com.training.warehouse.dto.request.CreateInboundRequest;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.training.warehouse.service.FileStoreService.ALLOWED_CONTENT_TYPES;

public class DtoValidationService {

    public static void validateCreateInboundRequest(CreateInboundRequest request) {
        List<MultipartFile> attachments = request.getAttachments();
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
