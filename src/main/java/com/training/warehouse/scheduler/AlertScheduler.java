package com.training.warehouse.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.repository.projection.LateOutboundProjection;
import com.training.warehouse.repository.projection.UnconfirmedOutboundsInLastSevenDaysProjection;
import com.training.warehouse.service.MailService;

@Component
@AllArgsConstructor
@Slf4j
public class AlertScheduler {
    private final MailService mailService;
    private final OutboundRepository outboundRepository;

    // @Scheduled(cron = "second minute hour dayOfMonth month dayOfWeek")

    // @Scheduled(cron = "0 0 18 * * *")
    @Scheduled(cron = "*/30 * * * * *")
    public void alertOutboundsNearDueDate() {
        log.info("start running alert outbound near due date job");
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);
        long page = 1;
        long limit = 20;
        while (true) {
            List<UnconfirmedOutboundsInLastSevenDaysProjection> outbounds = this.outboundRepository.findUnconfirmedOutboundsInLastSevenDaysNative(
                from, to, limit, (page - 1) * limit
            );
            outbounds.stream().forEach((e) -> {
                this.mailService.sendAsync(
                    e.getUserEmail(), 
                    "Alert outbound ", 
                    "Outbound %d must be export on %d/%d/%d".formatted(
                        e.getOutboundId(), 
                        e.getExpectedShippingDate().getDayOfMonth(),
                        e.getExpectedShippingDate().getMonthValue(),
                        e.getExpectedShippingDate().getYear()
                    )
                );
            });
            if (outbounds.size() < limit) {
                break;
            }
            page++;
        }
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void alertLateOutbounds() {
        log.info("start running alert late outbound job");
        long page = 1;
        long limit = 20;
        while (true) {
            List<LateOutboundProjection> outbounds = this.outboundRepository.findLateOutboundsNative(limit, (page - 1) * limit);
            outbounds.stream().forEach((e) -> {
                this.mailService.sendAsync(
                    e.getUserEmail(), 
                    "Alert outbound ", 
                    "Outbound %d is not exported, due date on %d/%d/%d".formatted(
                        e.getOutboundId(), 
                        e.getExpectedShippingDate().getDayOfMonth(),
                        e.getExpectedShippingDate().getMonthValue(),
                        e.getExpectedShippingDate().getYear()
                    )
                );
            });
            if (outbounds.size() < limit) {
                break;
            }
            page++;
        }
    }
}