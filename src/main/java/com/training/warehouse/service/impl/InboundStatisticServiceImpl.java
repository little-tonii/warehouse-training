package com.training.warehouse.service.impl;

import com.training.warehouse.dto.response.InboundSummaryMonthProjection;
import com.training.warehouse.dto.response.InboundSummaryPerMonth;
import com.training.warehouse.dto.response.InboundSummaryResponse;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.service.InboundStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class InboundStatisticServiceImpl implements InboundStatisticService {
    private final InboundRepository inboundRepository;

    @Override
    public Page<InboundSummaryResponse> getInboundSummary(Pageable pageable){
        return inboundRepository.findInboundSummaryByProductTypeAndSupplierCd(pageable);

    }

    @Override
    public List<InboundSummaryMonthProjection> getInboundSummaryByMonth(Integer startMonth, Integer endMonth, int year) {
        return inboundRepository.findInbSummaryByMonth(startMonth,endMonth,year);
    }

}
