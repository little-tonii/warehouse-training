package com.training.warehouse.service;

import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;

public interface InboundService {
    InboundResponse createInbound(InboundCreateRequest request);
    InboundResponse updateInbound(Long id, InboundUpdateRequest request);
    void deleteInbound(Long id);
}
