package com.training.warehouse.service;

import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;

public interface InboundService {
    void deleteInboundById(long inboundId);
    InboundResponse updateInbound(Long id, InboundUpdateRequest request);
}
