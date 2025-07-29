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

import com.training.warehouse.service.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

import lombok.AllArgsConstructor;

import java.util.*;

import com.training.warehouse.dto.response.RiskDelayedOutboundsProjection;


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
    private final MailService mailService;

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
}
