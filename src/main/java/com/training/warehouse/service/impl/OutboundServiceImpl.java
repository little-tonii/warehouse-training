package com.training.warehouse.service.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.training.warehouse.common.util.SecurityUtil;
import com.training.warehouse.dto.response.RiskDelayedOutboundsProjection;
import com.training.warehouse.dto.response.StockProjection;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.enumeric.ShippingMethod;
import com.training.warehouse.repository.UserRepository;
import com.training.warehouse.service.MailService;
import org.springframework.stereotype.Service;

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
import com.training.warehouse.service.OutboundService;
import com.training.warehouse.service.PdfService;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class OutboundServiceImpl implements OutboundService {
    private final OutboundRepository outboundRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final InboundRepository inboundRepository;
//    private final InboundAttachmentRepository inboundAttachmentRepository;
//    private final FileStoreService fileStoreService;
//    private final PdfService pdfService;

    @Override
    public byte[] confirmOutboundById(long outboundId) {
        return new byte[0];
    }

    @Override
    @Transactional
    public void alertDelayedOutbounds() {
        List<RiskDelayedOutboundsProjection> delayedOutbounds = outboundRepository.findAllRiskDelayedOutbounds();
        Map<String, List<RiskDelayedOutboundsProjection>> groupedByUser = delayedOutbounds.stream()
                .collect(Collectors.groupingBy(RiskDelayedOutboundsProjection::getUserEmail));

        for (Map.Entry<String, List<RiskDelayedOutboundsProjection>> entry : groupedByUser.entrySet()) {
            String userEmail = entry.getKey();
            List<RiskDelayedOutboundsProjection> userOutbounds = entry.getValue();

            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("Bạn có các đơn outbound có nguy cơ trễ:\n\n");

            for (RiskDelayedOutboundsProjection outbound : userOutbounds) {
                String invoice = inboundRepository.findById(outbound.getInboundID()).orElseThrow().getInvoice();
                contentBuilder.append("Dự kiến xuất: ").append(outbound.getExpectedShippingDate())
                        .append(" từ inbound có invoice: ").append(invoice).append("\n");
            }

            String content = contentBuilder.toString();
            mailService.sendMail(userEmail, "Risk Delayed Outbound", content);
            System.out.println("Gửi mail tới user: " + userEmail);
        }
    }

    @Override
    public StockProjection getStockSummaryByMonth(int month, int year) {
        return outboundRepository.findAllDiffQuantity(month, year);
    }

    @Override
    @Transactional
    public Map<String, Object> importCsvExportPlan(MultipartFile file) {
        List<OutboundEntity> validEntities = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        UserEntity currUser = SecurityUtil.getCurrentUser();

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

//
//    @Override
//    public byte[] confirmOutboundById(long outboundId) {
//        Optional<OutboundEntity> outboundResult = this.outboundRepository.findById(outboundId);
//        if (!outboundResult.isPresent()) {
//            throw new NotFoundException(ExceptionMessage.OUTBOUND_NOT_FOUND);
//        }
//        OutboundEntity outbound = outboundResult.get();
//        if (outbound.isConfirmed()) {
//            throw new BadRequestException(ExceptionMessage.OUTBOUND_CONFIRMED);
//        }
//        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(outbound.getInboundId());
//        if (!inboundResult.isPresent()) {
//            throw new NotFoundException(ExceptionMessage.INBOUND_NOT_FOUND);
//        }
//        List<InboundAttachmentEntity> inboundAttachments = this.inboundAttachmentRepository
//                .findByInboundId(outbound.getInboundId());
//        Map<String, byte[]> files = new HashMap<>();
//        inboundAttachments.forEach((attachment) -> {
//            byte[] file = fileStoreService.getFile(
//                    FileStoreService.INBOUND_BUCKET,
//                    attachment.getFilePath(),
//                    attachment.getFileName());
//            files.put(attachment.getFileName(), file);
//        });
//        byte[] mergedFile = this.pdfService.mergeWithBookmarks(files);
//        outbound.setConfirmed(true);
//        this.outboundRepository.save(outbound);
//        return mergedFile;
//    }
}
