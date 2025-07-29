package com.training.warehouse.service.impl;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.training.warehouse.dto.request.CreateOutboundRequest;
import com.training.warehouse.dto.response.CreateOutboundResponse;
import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.entity.OutboundAttachmentEntity;
import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.enumeric.ShippingMethod;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.exception.handler.ExceptionMessage;
import com.training.warehouse.repository.InboundAttachmentRepository;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.repository.OutboundAttachmentRepository;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.service.ExcelService;
import com.training.warehouse.service.FileStoreService;
import com.training.warehouse.service.OutboundService;
import com.training.warehouse.service.PdfService;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class OutboundServiceImpl implements OutboundService {
    private final OutboundRepository outboundRepository;
    private final InboundRepository inboundRepository;
    private final InboundAttachmentRepository inboundAttachmentRepository;
    private final FileStoreService fileStoreService;
    private final PdfService pdfService;
    private final OutboundAttachmentRepository outboundAttachmentRepository;
    private final ExcelService excelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public byte[] confirmOutboundById(long outboundId) {
        Optional<OutboundEntity> outboundResult = this.outboundRepository.findById(outboundId);
        if (outboundResult.isEmpty()) {
            throw new NotFoundException(ExceptionMessage.OUTBOUND_NOT_FOUND);
        }
        OutboundEntity outbound = outboundResult.get();
        if (outbound.isConfirmed()) {
            throw new BadRequestException(ExceptionMessage.OUTBOUND_CONFIRMED);
        }
        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(outbound.getInboundId());
        if (inboundResult.isEmpty()) {
            throw new NotFoundException(ExceptionMessage.INBOUND_NOT_FOUND);
        }
        List<InboundAttachmentEntity> inboundAttachments = this.inboundAttachmentRepository
                .findByInboundId(outbound.getInboundId());
        Map<String, byte[]> files = new HashMap<>();
        inboundAttachments.forEach((attachment) -> {
            byte[] file = fileStoreService.getFile(
                    FileStoreService.INBOUND_BUCKET,
                    attachment.getFilePath(),
                    attachment.getFileName());
            files.put(attachment.getFileName(), file);
        });
        byte[] mergedFile = this.pdfService.mergeWithBookmarks(files);
        String filePath = UUID.randomUUID().toString();
        String fileName = String.format("outbound-%s-confirmed.pdf", outbound.getId());
        this.outboundAttachmentRepository
                .save(OutboundAttachmentEntity.builder()
                        .filePath(filePath)
                        .fileName(fileName)
                        .build());
        outbound.setConfirmed(true);
        outbound.setActualShippingDate(LocalDateTime.now());
        this.outboundRepository.save(outbound);
        this.fileStoreService.uploadFile(FileStoreService.OUTBOUND_BUCKET, filePath, fileName, mergedFile);
        return mergedFile;
    }

    @Override
    public byte[] getLateOutboundStatistics(LocalDateTime startMonth, LocalDateTime endMonth) {
        List<OutboundEntity> outboundResult = this.outboundRepository.findLateOutboundsCreatedBetween(startMonth,
                endMonth);
        Map<YearMonth, List<OutboundEntity>> groupedByMonth = outboundResult.stream()
                .collect(Collectors.groupingBy(outboundEntity -> YearMonth.from(outboundEntity.getCreatedAt())));
        List<String> headers = List.of("month", "id", "quantity", "expected", "actual");
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Number> pieData = new LinkedHashMap<>();
        YearMonth start = YearMonth.from(startMonth);
        YearMonth end = YearMonth.from(endMonth);
        YearMonth current = start;
        while (!current.isAfter(end)) {
            List<OutboundEntity> outbounds = groupedByMonth.getOrDefault(current, Collections.emptyList());
            pieData.put(current.toString(), outbounds.size());
            for (OutboundEntity outbound : outbounds) {
                int expectedDay = outbound.getExpectedShippingDate().getDayOfMonth();
                int expectedMonth = outbound.getExpectedShippingDate().getMonthValue();
                int expectedYear = outbound.getExpectedShippingDate().getYear();
                int actualDay = -1, actualMonth = -1, actualYear = -1;
                if (outbound.getActualShippingDate() != null) {
                    actualDay = outbound.getActualShippingDate().getDayOfMonth();
                    actualMonth = outbound.getActualShippingDate().getMonthValue();
                    actualYear = outbound.getActualShippingDate().getYear();
                }
                Map<String, Object> dataRow = new HashMap<>();
                dataRow.put("month", current.toString());
                dataRow.put("id", outbound.getId());
                dataRow.put("quantity", outbound.getQuantity());
                dataRow.put("expected", "%d/%d/%d".formatted(expectedDay, expectedMonth, expectedYear));
                dataRow.put("actual",
                        outbound.getActualShippingDate() != null
                                ? "%d/%d/%d".formatted(actualDay, actualMonth, actualYear)
                                : "");
                data.add(dataRow);
            }
            if (outbounds.isEmpty()) {
                Map<String, Object> emptyRow = new HashMap<>();
                emptyRow.put("month", current.toString());
                emptyRow.put("id", "");
                emptyRow.put("quantity", "");
                emptyRow.put("expected", "");
                emptyRow.put("actual", "");
                data.add(emptyRow);
            }
            current = current.plusMonths(1);
        }
        var workbook = this.excelService.createWorkbook("late-outbound", headers, data);
        this.excelService.addPieChart((XSSFWorkbook) workbook, "late-outbound", pieData);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            workbook.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public CreateOutboundResponse createOutbound(UserEntity user, CreateOutboundRequest request) {
        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(request.getInboundId());
        if (inboundResult.isEmpty()) {
            throw new BadRequestException("Inbound is not valid");
        }
        List<OutboundEntity> existsOutbounds = this.outboundRepository.findByInboundId(request.getInboundId());
        long totalOutboundQuantity = existsOutbounds.stream().mapToLong((e) -> e.getQuantity()).sum();
        if (request.getQuantity() > inboundResult.get().getQuantity() - totalOutboundQuantity) {
            throw new BadRequestException("Not enough quantity");
        }
        OutboundEntity newOutbound = this.outboundRepository.save(OutboundEntity.builder()
                .inboundId(request.getInboundId())
                .quantity(request.getQuantity())
                .shippingMethod(ShippingMethod.fromCode(request.getShippingMethod()))
                .expectedShippingDate(request.getExceptedShippingDate())
                .user(user)
                .build());
        return CreateOutboundResponse.builder().id(newOutbound.getId()).build();
    }

    @Override
    public void deleteOutboundById(long id) {
        Optional<OutboundEntity> outboundResult = this.outboundRepository.findById(id);
        if (outboundResult.isEmpty()) {
            throw new NotFoundException("outbound not found");
        }
        LocalDateTime now = LocalDateTime.now();
        OutboundEntity outbound = outboundResult.get();
        if (outbound.getActualShippingDate() != null) {
            throw new BadRequestException("can not delete outbound");
        }
        if (outbound.getActualShippingDate() == null && outbound.getExpectedShippingDate().isBefore(now)) {
            throw new BadRequestException("can not delete outbound");
        }
        this.outboundRepository.delete(outbound);
    }

    @Override
    @Transactional
    public Map<String, Object> importCsvExportPlan(MultipartFile file) {
        List<OutboundEntity> validEntities = new ArrayList<>();
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

            List<String> required = List.of("invoice", "quantity", "shippingmethod", "expectedshippingdate");
            for (String field : required) {
                if (!headerMap.containsKey(field)) {
                    throw new IllegalArgumentException("Thiếu cột bắt buộc: " + field);
                }
            }

            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);

                try {
                    String invoice = row[headerMap.get("invoice")].trim();
                    String quantityStr = row[headerMap.get("quantity")].trim();
                    String shippingMethodStr = row[headerMap.get("shippingmethod")].trim();
                    String expectedDateStr = row[headerMap.get("expectedshippingdate")].trim();

                    if (invoice.isEmpty() || quantityStr.isEmpty() || shippingMethodStr.isEmpty() || expectedDateStr.isEmpty()) {
                        throw new IllegalArgumentException("Dữ liệu không được để trống");
                    }

                    long quantity = Long.parseLong(quantityStr);
                    ShippingMethod shippingMethod = ShippingMethod.valueOf(shippingMethodStr.toUpperCase());

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
                    LocalDateTime expectedShippingDate = LocalDate.parse(expectedDateStr, formatter).atStartOfDay();


                    InboundEntity inbound = inboundRepository.findByInvoice(invoice)
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy invoice: " + invoice));

                    OutboundEntity outbound = OutboundEntity.builder()
                            .inboundId(inbound.getId())
                            .quantity(quantity)
                            .shippingMethod(shippingMethod)
                            .expectedShippingDate(expectedShippingDate)
                            .isConfirmed(false)
                            .user(currUser)
                            .build();

                    validEntities.add(outbound);
                } catch (Exception e) {
                    errors.add("Lỗi dòng " + (i + 1) + ": " + e.getMessage());
                }
            }

            if (errors.isEmpty()) {
                outboundRepository.saveAll(validEntities);
            }

        } catch (IOException | CsvException e) {
            throw new RuntimeException("Không đọc được file CSV", e);
        }

        Map<String, Object> result = new HashMap<>();
        if (errors.isEmpty()) {
            result.put("success", validEntities.size() + " dòng đã được import thành công.");
        } else {
            result.put("errorMessages", errors);
        }

        return result;
    }
}
