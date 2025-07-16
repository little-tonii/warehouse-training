package com.training.warehouse.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.training.warehouse.common.util.SecurityUtil;
import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.request.InboundImportFileRequest;
import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;
import java.util.List;
import java.util.Optional;

import com.training.warehouse.entity.UserEntity;
import org.springframework.stereotype.Service;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;
import com.training.warehouse.exception.InvalidInboundStatusException;
import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.repository.InboundAttachmentRepository;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.service.FileStoreService;
import com.training.warehouse.service.InboundService;
import jakarta.validation.ConstraintViolation;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final FileStoreService fileStoreService;
    private final InboundRepository inboundRepository;

    @Autowired
    private final Validator validator;

    private final OutboundRepository outboundRepository;
    private final InboundAttachmentRepository inboundAttachmentRepository;

    private final Path rootDir = Paths.get("uploads/inbound");

    @Override
    public Map<String, Object> importFromCsv(MultipartFile file) {
        List<InboundEntity> validEntities = new ArrayList<>();
        List<String> errors = new ArrayList<>();
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

                    InboundEntity entity = InboundEntity.builder()
                            .invoice(dto.getInvoice())
                            .status(dto.getStatus())
                            .supplierCd(SupplierCd.fromCode(dto.getSupplierCd()))
                            .quantity(dto.getQuantity())
                            .productType(ProductType.fromString(dto.getProductType()))
                            .receiveDate(dto.getReceiveDate())
                            .build();

                    validEntities.add(entity);
                } catch (Exception e) {
                    errors.add("Lỗi dòng " + (i + 1) + ": " + e.getMessage());
                }
            }

            try {
                if (!validEntities.isEmpty()) {
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
        result.put("successCount", validEntities.size());
        result.put("errorMessages", errors);

        return result;
    }

    @Override
    @Transactional
    public InboundResponse createInbound(InboundCreateRequest dto) {
        UserEntity currUser = SecurityUtil.getCurrentUser();
        try {
            List<MultipartFile> attachments = dto.getAttachments();

            InboundEntity entity = InboundEntity.builder()
                    .invoice(dto.getInvoice())
                    .productType(dto.getProductType())
                    .supplierCd(dto.getSupplierCd())
                    .receiveDate(dto.getReceiveDate())
                    .status(OrderStatus.NOT_EXPORTED)
                    .quantity(dto.getQuantity())
                    .user(currUser)
                    .build();

            InboundEntity saved = inboundRepository.save(entity);
            Long savedId = saved.getId();
//            uploadFile(savedId, attachments);
            if(attachments.size() > 5){
                throw new IllegalArgumentException("Maximum 5 files");
            }
            for(MultipartFile file : attachments){
                String originFileName = file.getOriginalFilename();
                fileStoreService.uploadFile(FileStoreService.INBOUND_BUCKET,String.valueOf(savedId),file);
                InboundAttachmentEntity inboundAttachment = InboundAttachmentEntity.builder()
                        .fileName(originFileName)
                        .inboundId(savedId)
                        .filePath(String.valueOf(savedId)+"/"+file.getName())
                        .build();
                inboundAttachmentRepository.save(inboundAttachment);
            }

            return mapToResponse(saved);
        }catch (Exception e){
            throw new RuntimeException("Tạo đơn nhập thất bại: " + e.getMessage(), e);
        }
    }

    @Override
    public InboundResponse updateInbound(Long id, InboundUpdateRequest dto) {
        InboundEntity entity = inboundRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inbound not found"));

        if (entity.getStatus() != OrderStatus.NOT_EXPORTED) {
//            if (dto.getStatus() == OrderStatus.PARTIALLY_EXPORTED || dto.getStatus() == OrderStatus.FULLY_EXPORTED) {
//                entity.setStatus(dto.getStatus());
//                return mapToResponse(inboundRepository.save(entity));
//
//            }
            throw new InvalidInboundStatusException("Cannot update. Inbound status is not editable.");
        }

        setIfNotNull(dto.getInvoice(), entity::setInvoice);
        setIfNotNull(dto.getProductType(), entity::setProductType);
        setIfNotNull(dto.getSupplierCd(), entity::setSupplierCd);
        setIfNotNull(dto.getReceiveDate(), entity::setReceiveDate);
        setIfNotNull(dto.getQuantity(), entity::setQuantity);
        setIfNotNull(dto.getStatus(), entity::setStatus);

        return mapToResponse(inboundRepository.save(entity));
    }


    @Override
    public void deleteInboundById(long inboundId) {
        Optional<InboundEntity> inboundResult  = inboundRepository.findById(inboundId);
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
            fileStoreService.deleteFile(FileStoreService.INBOUND_BUCKET, attachment.getFilePath(), attachment.getFileName());
            inboundAttachmentRepository.deleteById(attachment.getId());
        });
        inboundRepository.deleteById(inboundId);
        return;
    }

    private InboundResponse mapToResponse(InboundEntity e) {

        return InboundResponse.builder()
                .id(e.getId())
                .invoice(e.getInvoice())
                .productType(e.getProductType())
                .supplierCd(e.getSupplierCd())
                .receiveDate(e.getReceiveDate())
                .quantity(e.getQuantity())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }
}