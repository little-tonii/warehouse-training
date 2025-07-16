package com.training.warehouse.service.impl;

import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
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

    @Override
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
        outbound.setConfirmed(true);
        this.outboundRepository.save(outbound);
        return mergedFile;
    }

    @Override
    public byte[] getLateOutboundStatistics(LocalDateTime startMonth, LocalDateTime endMonth) {
        List<OutboundEntity> outboundResult = this.outboundRepository.findLateOutboundsCreatedBetween(startMonth,
                endMonth);
        Map<YearMonth, List<OutboundEntity>> groupedByMonth = outboundResult.stream()
                .collect(Collectors.groupingBy(outboundEntity -> YearMonth.from(outboundEntity.getCreatedAt())));
        Workbook workbook = this.excelService.createWorkbook();
        List<String> headers = List.of("id", "quantity", "expected", "actual");
        Map<String, Number> pieData = new HashMap<>();
        for (var outbounds: groupedByMonth.entrySet()) {
            pieData.put(outbounds.getKey().toString(), outbounds.getValue().size());
            List<Map<String, Object>> data = new ArrayList<>();
            for (var outbound: outbounds.getValue()) {
                Map<String, Object> dataRow = new HashMap<>();
                dataRow.put("id", outbound.getId());
                dataRow.put("quantity", outbound.getQuantity());
                dataRow.put("expected", outbound.getExpectedShippingDate());
                dataRow.put("actual", outbound.getActualShippingDate());
                data.add(dataRow);
            }
            this.excelService.addSheetToWorkbook(workbook, outbounds.getKey().toString(), headers, data);
        }
        this.excelService.addPieChartSheetToWorkBook(workbook, "Pie Chart", pieData);
        return this.excelService.writeWorkbookToBytes(workbook);
    }
}
