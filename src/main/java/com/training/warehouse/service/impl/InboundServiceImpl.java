package com.training.warehouse.service.impl;

import java.util.List;
import java.util.Optional;

import com.training.warehouse.exception.ConflicException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.training.warehouse.entity.InboundAttachmentEntity;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.exception.BadRequestException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.repository.InboundAttachmentRepository;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.service.FileStoreService;
import com.training.warehouse.service.InboundService;

import lombok.AllArgsConstructor;
import com.training.warehouse.dto.request.CreateInboundRequest;
import com.training.warehouse.dto.response.FileUploadResult;
import com.training.warehouse.dto.response.CreateInboundResponse;
import com.training.warehouse.entity.UserEntity;
import com.training.warehouse.enumeric.OrderStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Consumer;

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
    @Transactional
    public CreateInboundResponse createInbound(CreateInboundRequest dto) {
        DtoValidationService.validateCreateInboundRequest(dto);

        UserEntity currUser = (UserEntity) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        InboundEntity saved;
        Long savedId;

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

        try {
            saved = inboundRepository.save(entity);
            savedId = saved.getId();
        } catch (Exception e) {
            throw new ConflicException("Lỗi khi lưu đơn nhập: " + e.getMessage());
        }

        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (file.isEmpty()) continue;

                String originFileName = file.getOriginalFilename();
                String fileKey = UUID.randomUUID().toString();
                String filePath = savedId + "/" + fileKey;

                try {
                    fileStoreService.uploadFile(FileStoreService.INBOUND_BUCKET, filePath, file);

                    InboundAttachmentEntity inboundAttachment = InboundAttachmentEntity.builder()
                            .fileName(originFileName)
                            .inboundId(savedId)
                            .filePath(filePath)
                            .build();

                    inboundAttachmentRepository.save(inboundAttachment);

                } catch (Exception e) {
                    try {
                        fileStoreService.deleteFile(FileStoreService.INBOUND_BUCKET, filePath, originFileName);
                    } catch (Exception ex) {
                    }
                    throw new ConflicException("Upload hoặc lưu file thất bại: " + e.getMessage());
                }
            }
        }

        return mapToResponse(saved);
    }

    private CreateInboundResponse mapToResponse(InboundEntity e) {

        return CreateInboundResponse.builder()
                .id(e.getId())
                .build();
    }

    public static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }
}
