package com.training.warehouse.service;

import com.training.warehouse.dto.request.CreateInboundRequest;
import com.training.warehouse.dto.response.CreateInboundResponse;
import com.training.warehouse.entity.UserEntity;

public interface InboundService {
    void deleteInboundById(long inboundId);
    InboundResponse updateInbound(Long id, InboundUpdateRequest request);
    CreateInboundResponse createInbound(UserEntity user, CreateInboundRequest request);
}
