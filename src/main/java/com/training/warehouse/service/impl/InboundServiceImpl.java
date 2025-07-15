package com.training.warehouse.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.service.FileStoreService;
import com.training.warehouse.service.InboundService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class InboundServiceImpl implements InboundService{

    private final FileStoreService fileStoreService;
    private final InboundRepository inboundRepository;

    @Override
    public void deleteInboundById(long inboundId) {
        Optional<InboundEntity> inboundResult  = inboundRepository.findById(inboundId);
        
    }
    
}
