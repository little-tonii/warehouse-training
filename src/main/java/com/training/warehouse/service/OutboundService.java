package com.training.warehouse.service;

import com.training.warehouse.dto.response.StockProjection;
import com.training.warehouse.entity.OutboundEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;

import com.training.warehouse.dto.request.CreateOutboundRequest;
import com.training.warehouse.dto.response.CreateOutboundResponse;
import com.training.warehouse.entity.UserEntity;

public interface OutboundService {
    byte[] confirmOutboundById(long outboundId);

    void alertDelayedOutbounds();

    StockProjection getStockSummaryByMonth(int month,int year);

    Map<String, Object> importCsvExportPlan(MultipartFile file);

    byte[] getLateOutboundStatistics(LocalDateTime startMonth, LocalDateTime endMonth);

    CreateOutboundResponse createOutbound(UserEntity user, CreateOutboundRequest request);

    void deleteOutboundById(long id);

}
