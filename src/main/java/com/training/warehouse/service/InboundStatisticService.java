package com.training.warehouse.service;

import com.training.warehouse.dto.response.InboundSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InboundStatisticService {
    Page<InboundSummaryResponse> getInboundSummary(Pageable pageable);

    InboundSummaryResponse getInboundSummaryByMonth(int startMonth,int endMonth);
}
