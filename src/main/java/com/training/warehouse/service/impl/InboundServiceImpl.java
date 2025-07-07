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
    public InboundResponse createInbound(InboundRequest dto){
        InboundEntity entity = new InboundEntity();
        entity.setInvoice(dto.getInvoice());
        entity.setProductType(dto.getProductType());
        entity.setSupplierCd(dto.getSupplierCd()) ;
        entity.setReceiveDate(dto.getReceiveDate());
        entity.setStatus(OrderStatus.NOT_EXPORTED);
        entity.setQuantity(dto.getQuantity());
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        InboundEntity savedEntity = inboundRepository.save(entity);

        InboundResponse result = new InboundResponse();
        result.setId(savedEntity.getId());
        result.setInvoice(savedEntity.getInvoice());
        result.setProductType(savedEntity.getProductType());
        result.setSupplierCd(savedEntity.getSupplierCd());
        result.setReceiveDate(savedEntity.getReceiveDate());
        result.setQuantity(savedEntity.getQuantity());
        result.setStatus(savedEntity.getStatus());
        result.setCreatedAt(savedEntity.getCreatedAt());
        result.setUpdatedAt(savedEntity.getUpdatedAt());

        return result;
    }

    public InboundResponse updateInbound(InboundRequest dto){
        InboundEntity entity = inboundRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("Inbound not found"));

        entity.setId(dto.getId());
        entity.setInvoice(dto.getInvoice());
        entity.setProductType(dto.getProductType());
        entity.setSupplierCd(dto.getSupplierCd()) ;
        entity.setReceiveDate(dto.getReceiveDate());
        entity.setStatus(dto.getStatus());
        entity.setQuantity(dto.getQuantity());

        InboundEntity savedEntity = inboundRepository.save(entity);

        InboundResponse res = new InboundResponse();
        res.setId(savedEntity.getId());
        res.setInvoice(savedEntity.getInvoice());
        res.setProductType(savedEntity.getProductType());
        res.setSupplierCd(savedEntity.getSupplierCd());
        res.setReceiveDate(savedEntity.getReceiveDate());
        res.setQuantity(savedEntity.getQuantity());
        res.setStatus(savedEntity.getStatus());
        res.setCreatedAt(savedEntity.getCreatedAt());
        res.setUpdatedAt(savedEntity.getUpdatedAt());

        return res;
    }

    public void deleteInbound (InboundRequest dto){
        InboundEntity entity = inboundRepository.findById(dto.getId()).orElseThrow(() -> new RuntimeException("Inbound not found"));
        inboundRepository.delete(entity);
    }
}
