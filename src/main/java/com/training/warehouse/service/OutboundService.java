package com.training.warehouse.service;

import java.time.LocalDateTime;

import com.training.warehouse.dto.request.CreateOutboundRequest;
import com.training.warehouse.dto.response.CreateOutboundResponse;
import com.training.warehouse.entity.UserEntity;

public interface OutboundService {
    byte[] confirmOutboundById(long outboundId);

    byte[] getLateOutboundStatistics(LocalDateTime startMonth, LocalDateTime endMonth);

    CreateOutboundResponse createOutbound(UserEntity user, CreateOutboundRequest request);

    void deleteOutboundById(long id);

    void alertDelayedOutbounds();

}
