package com.training.warehouse.service;

import com.training.warehouse.dto.response.InboundSummaryMonthProjection;
import com.training.warehouse.dto.response.InboundSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InboundStatisticService {
    Page<InboundSummaryResponse> getInboundSummary(Pageable pageable);

    List<InboundSummaryMonthProjection> getInboundSummaryByMonth(int startMonth, int endMonth,int year);
}
