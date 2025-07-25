package com.training.warehouse.service.impl;

import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String filePath = UUID.randomUUID().toString();
        String fileName = String.format("outbound-%s-confirmed", outbound.getId());
        this.fileStoreService.uploadFile(FileStoreService.OUTBOUND_BUCKET, filePath, fileName, mergedFile);
        this.outboundAttachmentRepository
                .save(OutboundAttachmentEntity.builder()
                        .filePath(filePath)
                        .fileName(fileName)
                        .build());
        outbound.setConfirmed(true);
        this.outboundRepository.save(outbound);
        return mergedFile;
    }
}
