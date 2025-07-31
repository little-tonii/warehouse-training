package com.training.warehouse.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlertScheduler {

    @Scheduled(cron = "0 0 */6 * * *")
    public void checkAndSendDelayedOrders() {
        
    }
}