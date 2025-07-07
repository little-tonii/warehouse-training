package com.training.warehouse.service;

import com.training.warehouse.dto.request.InboundRequest;
import com.training.warehouse.dto.response.InboundResponse;

public interface InboundService {
    InboundResponse createInbound(InboundRequest dto);
    InboundResponse updateInbound(Long id,InboundRequest dto);
    void deleteInbound(Long id);
}
