package com.training.warehouse.service;

import com.training.warehouse.dto.request.InboundRequest;
import com.training.warehouse.dto.response.InboundResponse;

public interface InboundService {
    InboundResponse createInbound(InboundRequest request);
    InboundResponse updateInbound(Long id,InboundRequest request);
    void deleteInbound(Long id);
}
