package com.training.warehouse.service.impl;

import java.util.Optional;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.entity.OutboundAttachmentEntity;
import com.training.warehouse.entity.OutboundEntity;
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

@Service
@AllArgsConstructor
public class OutboundServiceImpl implements OutboundService {
    private final OutboundRepository outboundRepository;
    private final InboundRepository inboundRepository;
    private final InboundAttachmentRepository inboundAttachmentRepository;
    private final FileStoreService fileStoreService;
    private final PdfService pdfService;
    private final ExcelService excelService;
    private final OutboundAttachmentRepository outboundAttachmentRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public byte[] confirmOutboundById(long outboundId) {
        Optional<OutboundEntity> outboundResult = this.outboundRepository.findById(outboundId);
        if (!outboundResult.isPresent()) {
            throw new NotFoundException(ExceptionMessage.OUTBOUND_NOT_FOUND);
        }
        OutboundEntity outbound = outboundResult.get();
        if (outbound.isConfirmed()) {
            throw new BadRequestException(ExceptionMessage.OUTBOUND_CONFIRMED);
        }
        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(outbound.getInboundId());
        if (!inboundResult.isPresent()) {
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

        String filePath = "outbound-%d".formatted(outbound.getId());
        String fileName = "outbound-%d-confirm".formatted(outbound.getId());
        this.outboundAttachmentRepository.save(
                OutboundAttachmentEntity.builder()
                        .fileName(fileName)
                        .filePath(filePath)
                        .outboundId(outbound.getId())
                        .build());
        outbound.setConfirmed(true);
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
                Map<String, Object> dataRow = new HashMap<>();
                dataRow.put("month", current.toString());
                dataRow.put("id", outbound.getId());
                dataRow.put("quantity", outbound.getQuantity());
                dataRow.put("expected", outbound.getExpectedShippingDate());
                dataRow.put("actual", outbound.getActualShippingDate());
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
}
