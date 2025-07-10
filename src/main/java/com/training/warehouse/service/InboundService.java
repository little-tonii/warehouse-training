package com.training.warehouse.service;

import com.training.warehouse.dto.request.InboundCreateRequest;
import com.training.warehouse.dto.request.InboundUpdateRequest;
import com.training.warehouse.dto.response.InboundResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface InboundService {
    InboundResponse createInbound(InboundCreateRequest request);
    InboundResponse updateInbound(Long id, InboundUpdateRequest request);
    void deleteInbound(Long id);
    Map<String, Object> importFromCsv(MultipartFile file);
}
