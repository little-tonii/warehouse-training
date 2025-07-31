package com.training.warehouse.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.enumeric.OrderStatus;

import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.repository.InboundAttachmentRepository;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.service.FileStoreService;
import com.training.warehouse.service.InboundService;

import java.util.*;
import java.util.function.Consumer;


import lombok.AllArgsConstructor;
import com.training.warehouse.dto.request.CreateInboundRequest;
import com.training.warehouse.dto.request.UpdateInboundByIdRequest;
import com.training.warehouse.dto.response.CreateInboundResponse;
import com.training.warehouse.dto.response.UpdateInboundByIdResponse;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;

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
            throw new NotFoundException("inbound not found");
        }
        List<OutboundEntity> outboundEntities = outboundRepository.findByInboundId(inboundId);
        if (outboundEntities.size() > 0) {
            throw new BadRequestException("cannot delete inbound");
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
    @Transactional(rollbackFor = Exception.class)
    public CreateInboundResponse createInbound(UserEntity user, CreateInboundRequest request) {
        List<String> filePaths = new ArrayList<>();
        request.getAttachments().stream().forEach((e) -> {
            String path = UUID.randomUUID().toString();
            filePaths.add(path);
            this.fileStoreService.uploadFile(FileStoreService.INBOUND_BUCKET, path, e);
        });
        InboundEntity newInbound = this.inboundRepository.save(
            InboundEntity.builder()
                .invoice(request.getInvoice())
                .productType(ProductType.fromString(request.getProductType()))
                .supplierCd(SupplierCd.fromCode(request.getSupplierCd()))
                .receiveDate(request.getReceiveDate())
                .status(OrderStatus.fromValue(request.getOrderStatus()))
                .quantity(request.getQuantity())
                .user(user)
                .build()
        );
        for (int i = 0; i < request.getAttachments().size(); i++) {
            this.inboundAttachmentRepository.save(
                InboundAttachmentEntity.builder()
                    .fileName(request.getAttachments().get(i).getOriginalFilename())
                    .filePath(filePaths.get(i))
                    .build()
            );
        }
        return CreateInboundResponse.builder().id(newInbound.getId()).build();
    }
    
    @Override
    public UpdateInboundByIdRequest updateInboundById(long id, UpdateInboundByIdResponse request) {
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

   
}
