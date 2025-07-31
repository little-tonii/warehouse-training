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
                    .inboundId(newInbound.getId())
                    .build()
            );
        }
        return CreateInboundResponse.builder().id(newInbound.getId()).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateInboundByIdResponse updateInboundById(long id, UpdateInboundByIdRequest request) {
        Optional<InboundEntity> inboundResult = this.inboundRepository.findById(id);
        if (inboundResult.isEmpty()) {
            throw new NotFoundException("inbound not found");
        }
        InboundEntity inbound = inboundResult.get();
        Optional<OutboundEntity> outboundResult = this.outboundRepository.findFirstByInboundId(inbound.getId());
        if (outboundResult.isPresent()) {
            throw new BadRequestException("inbound is not editable");
        }
        List<InboundAttachmentEntity> inboundAttachments = this.inboundAttachmentRepository.findByInboundId(inbound.getId());
        if (inboundAttachments.size() + request.getAttachments().size() > 5) {
            throw new BadRequestException("too many attachments");
        }
        List<String> filePaths = new ArrayList<>();
        request.getAttachments().stream().forEach((e) -> {
            String path = UUID.randomUUID().toString();
            filePaths.add(path);
            this.fileStoreService.uploadFile(FileStoreService.INBOUND_BUCKET, path, e);
        });
        for (int i = 0; i < request.getAttachments().size(); i++) {
            this.inboundAttachmentRepository.save(
                InboundAttachmentEntity.builder()
                    .fileName(request.getAttachments().get(i).getOriginalFilename())
                    .filePath(filePaths.get(i))
                    .inboundId(inbound.getId())
                    .build()
            );
        }
        inbound.setInvoice(request.getInvoice());
        inbound.setProductType(ProductType.fromString(request.getProductType()));
        inbound.setSupplierCd(SupplierCd.fromCode(request.getSupplierCd()));
        inbound.setReceiveDate(request.getReceiveDate());
        inbound.setStatus(OrderStatus.fromValue(request.getOrderStatus()));
        inbound.setQuantity(request.getQuantity());
        InboundEntity updatedInbound = this.inboundRepository.save(inbound);
        return UpdateInboundByIdResponse.builder()
            .id(updatedInbound.getId())
            .build();
    }
}
