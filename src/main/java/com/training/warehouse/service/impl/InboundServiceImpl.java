package com.training.warehouse.service.impl;

import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.FileUploadResult;
import com.training.warehouse.dto.response.InboundResponse;

import java.util.List;
import java.util.Optional;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.enumeric.OrderStatus;

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

import java.util.*;
import java.util.function.Consumer;


import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final FileStoreService fileStoreService;
    private final InboundRepository inboundRepository;
    private final OutboundRepository outboundRepository;
    private final InboundAttachmentRepository inboundAttachmentRepository;

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
    public InboundResponse updateInbound(Long id, InboundUpdateRequest dto) {
        InboundEntity entity = inboundRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inbound not found"));
        dto.validate();
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

        List<FileUploadResult> results = new ArrayList<>();
        try {
            inboundRepository.save(entity);

            List<InboundAttachmentEntity> inboundAttachments = entity.getAttachments();
            if (inboundAttachments != null && !inboundAttachments.isEmpty()) {
                int i = 0;
                for (InboundAttachmentEntity inboundAttachment : inboundAttachments) {
                    FileUploadResult result = new FileUploadResult();
                    if (dto.getAttachments().get(i) != null && !dto.getAttachments().get(i).isEmpty()) {
                        Long inbId = inboundAttachment.getInboundId();
                        String filePath = inboundAttachment.getFilePath();
                        String fileName = inboundAttachment.getFileName();

                        String newFileName = dto.getAttachments().get(i).getOriginalFilename();
                        result.setFileName(newFileName);
                        try {
                            try { // k tồn tại file
                                fileStoreService.deleteFile(FileStoreService.INBOUND_BUCKET, filePath, fileName);
                            } catch (Exception e) {
                                // k tồn tại file thì lỗi, bỏ qua lỗi
                            }

                            String fileKey = UUID.randomUUID().toString() + "_" + newFileName;
                            String newFilePath = inbId + "/" + fileKey;
                            try {
                                fileStoreService.uploadFile(FileStoreService.INBOUND_BUCKET, newFilePath, dto.getAttachments().get(i));
                                inboundAttachment.setFileName(newFileName);
                                inboundAttachment.setFilePath(newFilePath);
                                inboundAttachmentRepository.save(inboundAttachment);
                            } catch (Exception e) {
                                result.setUploaded(false);
                            }
                        } catch (Exception e) {
                            result.setUploaded(false);
                            result.setSavedToDB(false);
                            result.setErrorMessage("Cannot save file " + newFileName);
                        }
                    }
                    i++;
                    results.add(result);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mapToResponse(inboundRepository.save(entity), results);
    }

    private InboundResponse mapToResponse(InboundEntity e, List<FileUploadResult> results) {

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
                .results(results)
                .build();
    }

    public static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }
}
