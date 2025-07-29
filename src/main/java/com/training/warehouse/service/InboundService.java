package com.training.warehouse.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface InboundService {
    void deleteInboundById(long inboundId);
    Map<String, Object> importFromCsv(MultipartFile file);
}
