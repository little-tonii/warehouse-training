package com.training.warehouse.service;

import com.training.warehouse.dto.request.ReportMonthlyRequest;
import com.training.warehouse.dto.response.ReportMonthlyResponse;

public interface ReportService {
    ReportMonthlyResponse getReportMonthly(ReportMonthlyRequest request);    
} 