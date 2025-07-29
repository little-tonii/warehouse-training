package com.training.warehouse.service.impl;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.repository.InboundAttachmentRepository;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.service.FileStoreService;
import com.training.warehouse.service.InboundService;

import lombok.AllArgsConstructor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.training.warehouse.dto.request.InboundImportFileRequest;

import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;

import jakarta.validation.ConstraintViolation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final FileStoreService fileStoreService;
    private final InboundRepository inboundRepository;
    private final OutboundRepository outboundRepository;
    private final InboundAttachmentRepository inboundAttachmentRepository;

    @Autowired
    private final Validator validator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInboundById(long inboundId) {
        Optional<InboundEntity> inboundResult = inboundRepository.findById(inboundId);
        if (!inboundResult.isPresent()) {
            throw new NotFoundException(ExceptionMessage.INBOUND_NOT_FOUND);
        }
        List<OutboundEntity> outboundEntities = outboundRepository.findByInboundId(inboundId);
        if (outboundEntities.size() > 0) {
            throw new BadRequestException(ExceptionMessage.CANNOT_DELETE_INBOUND);
        }
        InboundEntity inbound = inboundResult.get();
        List<InboundAttachmentEntity> attachments = inbound.getAttachments();
        attachments.forEach(attachment -> {
            inboundAttachmentRepository.deleteById(attachment.getId());
        });
        inboundRepository.deleteById(inboundId);
        attachments.forEach(attachment -> {
            fileStoreService.deleteFile(FileStoreService.INBOUND_BUCKET, attachment.getFilePath(),
                    attachment.getFileName());
        });
        return;
    }

    @Override
    public Map<String, Object> importFromCsv(MultipartFile file) {
        List<InboundEntity> validEntities = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        UserEntity currUser = (UserEntity) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {
            List<String[]> allRows = csvReader.readAll();

            if (allRows.isEmpty()) throw new IllegalArgumentException("File is empty");

            String[] headers = allRows.get(0);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase().replaceAll("\\s+", "");
                headerMap.put(header, i);
            }

            List<String> required = List.of("suppliercountry", "invoice", "producttype", "quantity", "receivedate");
            for (String field : required) {
                if (!headerMap.containsKey(field)) {
                    throw new IllegalArgumentException("Missing Column" + field);
                }
            }

            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);

                try {
                    InboundImportFileRequest dto = new InboundImportFileRequest();

                    dto.setInvoice(row[headerMap.get("invoice")]);
                    dto.setSupplierCd(row[headerMap.get("suppliercountry")]);
                    dto.setProductType(row[headerMap.get("producttype")]);
                    dto.setQuantity(Long.parseLong(row[headerMap.get("quantity")]));

                    String dateStr = row[headerMap.get("receivedate")];
                    if (dateStr != null && !dateStr.isBlank()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
                        LocalDateTime receiveDate = LocalDate.parse(dateStr, formatter).atStartOfDay();
                        dto.setReceiveDate(receiveDate);
                    }

                    dto.setStatus(OrderStatus.NOT_EXPORTED);

                    Set<ConstraintViolation<InboundImportFileRequest>> violations = validator.validate(dto);

                    if (!violations.isEmpty()) {
                        String message = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining("; "));
                        throw new IllegalArgumentException(message);
                    }

                    if (inboundRepository.findByInvoice(dto.getInvoice()).isPresent()) {
                        throw new BadRequestException("Invoice đã tồn tại");
                    }

                    InboundEntity entity = InboundEntity.builder()
                            .invoice(dto.getInvoice())
                            .status(dto.getStatus())
                            .supplierCd(SupplierCd.fromCode(dto.getSupplierCd()))
                            .quantity(dto.getQuantity())
                            .productType(ProductType.fromString(dto.getProductType()))
                            .receiveDate(dto.getReceiveDate())
                            .user(currUser)
                            .build();

                    validEntities.add(entity);
                } catch (Exception e) {
                    errors.add("Lỗi dòng " + (i + 1) + ": " + e.getMessage());
                }
            }

            try {
                if (errors.isEmpty()) {
                    inboundRepository.saveAll(validEntities);
                }
            } catch (DataIntegrityViolationException e) {
                throw new RuntimeException("Lỗi lưu dữ liệu: " + e.getMessage(), e);
            }

            if (!errors.isEmpty()) {
                errors.add("Import completed with some errors:\n" + String.join("\n", errors));
            }
        } catch (IOException | CsvException e) {
            throw new RuntimeException("Failed to read CSV file", e);
        }
        Map<String, Object> result = new HashMap<>();
        if(errors.isEmpty())
            result.put("success", validEntities.size() + " hàng được import");
        else result.put("errorMessages", errors);

        return result;
    }
}
