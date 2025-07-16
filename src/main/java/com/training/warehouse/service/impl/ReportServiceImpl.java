package com.training.warehouse.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.training.warehouse.dto.request.ReportMonthlyRequest;
import com.training.warehouse.dto.response.ReportMonthlyResponse;
import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.repository.InboundRepository;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.service.ReportService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReportServiceImpl implements ReportService {

//    private final InboundRepository inboundRepository;
//    private final OutboundRepository outboundRepository;

    @Override
    public ReportMonthlyResponse getReportMonthly(ReportMonthlyRequest request) {
//        List<InboundEntity> inbounds = this.inboundRepository.findAll(
//            PageRequest.of(request.getPage() - 1, request.getLimit())).getContent();
//        LocalDateTime startOfMonth = LocalDateTime.of(request.getYear(), request.getMonth(), 1, 0, 0);
//        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);
//        List<ReportMonthlyResponse.ReportMonthlyData> reportData = inbounds.stream()
//            .map((inbound) -> {
//                List<OutboundEntity> outboundsInMonth = outboundRepository
//                    .findByInboundIdAndShippingDateBetween(inbound.getId(), startOfMonth, endOfMonth);
//                long outboundQuantityInMonth = outboundsInMonth.stream().mapToLong(OutboundEntity::getQuantity).sum();
//                List<OutboundEntity> outboundsBeforeMonth = outboundRepository
//                    .findByInboundIdAndShippingDateBefore(inbound.getId(), startOfMonth);
//                long quantityOutBeforeMonth = outboundsBeforeMonth.stream().mapToLong(OutboundEntity::getQuantity).sum();
//                long quantityBeginMonth = inbound.getQuantity() - quantityOutBeforeMonth;
//                long quantityEndMonth = inbound.getQuantity() - outboundQuantityInMonth - quantityOutBeforeMonth;
//                long differenceMonthQuantity = quantityEndMonth - quantityBeginMonth;
//                return ReportMonthlyResponse.ReportMonthlyData.builder()
//                    .inbound_id(inbound.getId())
//                    .invoice(inbound.getInvoice())
//                    .productType(inbound.getProductType().getName())
//                    .supplierCd(inbound.getSupplierCd().getCode())
//                    .receiveDate(inbound.getReceiveDate())
//                    .status(inbound.getStatus().getValue())
//                    .quantity(inbound.getQuantity())
//                    .beginMonthQuantity(quantityBeginMonth)
//                    .endMonthQuantity(quantityEndMonth)
//                    .differenceMonthQuantity(differenceMonthQuantity)
//                    .createdAt(inbound.getCreatedAt())
//                    .updatedAt(inbound.getUpdatedAt())
//                    .build();
//            })
//            .collect(Collectors.toList());
//        return ReportMonthlyResponse.builder()
//            .year(request.getYear())
//            .month(request.getMonth())
//            .page(request.getPage())
//            .limit(request.getLimit())
//            .total(reportData.size())
//            .monthlyData(reportData)
//            .build();
//    }
    return  null;
    }

}
