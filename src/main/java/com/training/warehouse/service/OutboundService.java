package com.training.warehouse.service;

import java.time.LocalDateTime;

public interface OutboundService {
    byte[] confirmOutboundById(long outboundId);
    byte[] getLateOutboundStatistics(LocalDateTime startMonth, LocalDateTime endMonth);
    
}
