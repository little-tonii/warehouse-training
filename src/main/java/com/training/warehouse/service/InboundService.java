package com.training.warehouse.service;

import com.training.warehouse.dto.request.CreateInboundRequest;
import com.training.warehouse.dto.response.CreateInboundResponse;

public interface InboundService {
    void deleteInboundById(long inboundId);
    CreateInboundResponse createInbound(CreateInboundRequest request);
}
