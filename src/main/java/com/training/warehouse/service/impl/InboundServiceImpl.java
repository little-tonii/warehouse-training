package com.training.warehouse.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;
import com.training.warehouse.exception.InvalidInboundStatusException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.service.InboundService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundRepository inboundRepository;

    @Autowired
    private Validator validator;

    @Override
    public void importFromCsv(MultipartFile file) {
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
                String row[] = allRows.get(i);

                try {
                    InboundCreateRequest dto = new InboundCreateRequest();

                    dto.setInvoice(row[headerMap.get("invoice")]);
                    dto.setSupplierCd(SupplierCd.fromCode(row[headerMap.get("suppliercountry")]));
                    dto.setProductType(ProductType.fromString(row[headerMap.get("producttype")]));
                    dto.setQuantity(Long.parseLong(row[headerMap.get("quantity")]));

                    String dateStr = row[headerMap.get("receivedate")];
                    if (dateStr != null && !dateStr.isBlank()) {
                        dto.setReceiveDate(LocalDateTime.parse(dateStr));
                    }

                    dto.setStatus(OrderStatus.NOT_EXPORTED);

                    // 🧠 Validate toàn bộ DTO
                    Set<ConstraintViolation<InboundCreateRequest>> violations = validator.validate(dto);

                    if (!violations.isEmpty()) {
                        String message = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining("; "));
                        throw new IllegalArgumentException(message);
                    }


                    // Chuyển sang Entity nếu valid
                    InboundEntity entity = new InboundEntity();
                    entity.builder()
                            .invoice(dto.getInvoice())
                            .status(dto.getStatus())
                            .supplierCd(dto.getSupplierCd())
                            .quantity(dto.getQuantity())
                            .productType(dto.getProductType())
                            .receiveDate(dto.getReceiveDate())
                            .build();

//                    validEntities.add(entity);

                } catch (Exception e) {

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InboundResponse createInbound(InboundCreateRequest dto) {
        InboundEntity entity = InboundEntity.builder()
                .invoice(dto.getInvoice())
                .productType(dto.getProductType())
                .supplierCd(dto.getSupplierCd())
                .receiveDate(dto.getReceiveDate())
                .status(OrderStatus.NOT_EXPORTED)
                .quantity(dto.getQuantity())
                .build();

        InboundEntity saved = inboundRepository.save(entity);
        return mapToResponse(saved);
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
    public void deleteInbound(Long id) {
        InboundEntity entity = inboundRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inbound ${id} not exist"));

        if (entity.getStatus() != OrderStatus.NOT_EXPORTED) {
            throw new InvalidInboundStatusException("Cannot delete. Inbound status is not editable.");
        }

        inboundRepository.delete(entity);
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