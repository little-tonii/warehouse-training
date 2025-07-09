package com.training.warehouse.service;

import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;
import org.springframework.web.multipart.MultipartFile;

public interface InboundService {
    InboundResponse createInbound(InboundCreateRequest request);
    InboundResponse updateInbound(Long id, InboundUpdateRequest request);
    void deleteInbound(Long id);
    void importFromCsv(MultipartFile file);
}
