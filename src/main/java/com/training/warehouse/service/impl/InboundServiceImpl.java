package com.training.warehouse.service.impl;

import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.exception.InvalidInboundStatusException;
import com.training.warehouse.exception.NotFoundException;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundRepository inboundRepository;

    @Override
    public InboundResponse createInbound(InboundCreateRequest dto) {
        InboundEntity entity = InboundEntity.builder()
                .invoice(dto.getInvoice())
                .productType(dto.getProductType())
                .supplierCd(dto.getSupplierCd())
                .receiveDate(dto.getReceiveDate())
                .status(OrderStatus.NOT_EXPORTED)
                .quantity(dto.getQuantity())
                .build();

        InboundEntity saved = inboundRepository.save(entity);
        return mapToResponse(saved);
    }

    @Override
    public InboundResponse updateInbound(Long id, InboundUpdateRequest dto) {
        InboundEntity entity = inboundRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inbound not found"));

        if (entity.getStatus() != OrderStatus.NOT_EXPORTED) {
            if (dto.getStatus() == OrderStatus.PARTIALLY_EXPORTED || dto.getStatus() == OrderStatus.FULLY_EXPORTED) {
                entity.setStatus(dto.getStatus());
                return mapToResponse(inboundRepository.save(entity));

            }
            else throw new InvalidInboundStatusException("Cannot update. Inbound status is not editable.");
        }

        setIfNotNull(dto.getInvoice(),entity::setInvoice);
        setIfNotNull(dto.getProductType(),entity::setProductType);
        setIfNotNull(dto.getSupplierCd(),entity::setSupplierCd);
        setIfNotNull(dto.getReceiveDate(),entity::setReceiveDate);
        setIfNotNull(dto.getQuantity(),entity::setQuantity);
        setIfNotNull(dto.getStatus(),entity::setStatus);

        return mapToResponse(inboundRepository.save(entity));
    }

    @Override
    public void deleteInbound(Long id) {
        InboundEntity entity = inboundRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inbound ${id} not exist"));

        if (entity.getStatus() != OrderStatus.NOT_EXPORTED) {
            throw new InvalidInboundStatusException("Cannot delete. Inbound status is not editable.");
        }

        inboundRepository.delete(entity);
    }

    private InboundResponse mapToResponse(InboundEntity e) {
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
                .build();
    }

    public static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }
}