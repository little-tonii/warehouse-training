package com.training.warehouse.service.impl;

import com.training.warehouse.dto.request.InboundRequest;
import com.training.warehouse.dto.response.InboundResponse;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundRepository inboundRepository;

    @Override
    public InboundResponse createInbound(InboundRequest dto) {
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
    public InboundResponse updateInbound(Long id, InboundRequest dto) {
        InboundEntity entity = inboundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inbound not found"));

        if (entity.getStatus() != OrderStatus.NOT_EXPORTED) {
            throw new RuntimeException("Cannot update. Inbound status is not editable.");
        }

        entity.setInvoice(dto.getInvoice());
        entity.setProductType(dto.getProductType());
        entity.setSupplierCd(dto.getSupplierCd());
        entity.setReceiveDate(dto.getReceiveDate());
        entity.setQuantity(dto.getQuantity());
        entity.setStatus(dto.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(inboundRepository.save(entity));
    }

    @Override
    public void deleteInbound(Long id) {
        InboundEntity entity = inboundRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inbound ${id} not exist"));

        if (entity.getStatus() != OrderStatus.NOT_EXPORTED) {
            throw new RuntimeException("Cannot delete. Inbound status is not editable.");
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
}