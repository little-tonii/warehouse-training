package com.training.warehouse.service;

import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.response.InboundResponse;

public interface InboundService {
    void deleteInboundById(long inboundId);
    InboundResponse createInbound(InboundCreateRequest request);
}
