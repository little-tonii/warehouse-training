package com.training.warehouse.scheduler;

import com.training.warehouse.service.OutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DelayedOutboundJob {

    private final OutboundService outboundService;

    @Scheduled(cron = "*/30 * * * * *")
    public void checkAndSendDelayedOrders() {
        outboundService.alertDelayedOutbounds();
    }
}