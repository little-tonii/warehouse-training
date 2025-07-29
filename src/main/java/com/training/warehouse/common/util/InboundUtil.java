package com.training.warehouse.common.util;

import com.training.warehouse.dto.response.InboundSummaryMonthProjection;
import com.training.warehouse.dto.response.InboundSummaryPerMonth;

import java.util.*;

public class InboundUtil {
    public static Map<Integer, List<InboundSummaryPerMonth>> extractAndGroupDataByMonth(List<InboundSummaryMonthProjection> dataSummary) {
        if (dataSummary == null) return null;
        Map<Integer, List<InboundSummaryPerMonth>> groupedDataByMonth = new LinkedHashMap<>();

        for (InboundSummaryMonthProjection inb : dataSummary) {
            Integer month = inb.getMonth();
            if (!groupedDataByMonth.containsKey(month)) {
                groupedDataByMonth.put(month, new ArrayList<>());
            }

            groupedDataByMonth.get(month).add(new InboundSummaryPerMonth(inb.getMonth(), inb.getProductType(), inb.getSupplierCd(), inb.getTotalQuantity()));
        }
        return groupedDataByMonth;
    }

    public static Map<String,Map<String, Long>> groupDataBySupplierAndProductType(List<InboundSummaryPerMonth> data){
        Map<String,Map<String,Long>> groupedData = new LinkedHashMap<>();
        for (InboundSummaryPerMonth dataMonth : data) {
            String supplierCode = dataMonth.getSupplierCd();
            if (!groupedData.containsKey(supplierCode)) groupedData.put(supplierCode, new TreeMap<>());

            Map<String, Long> productMap = groupedData.get(supplierCode);
            String productType = dataMonth.getProductType();
            productMap.put(productType, productMap.getOrDefault(productType, 0L) + dataMonth.getTotalQuantity());
        }
        return groupedData;
    }
}
