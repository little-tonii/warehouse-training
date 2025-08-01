package com.training.warehouse.scheduler;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.repository.OutboundRepository;
import com.training.warehouse.service.MailService;

@Component
@RequiredArgsConstructor
public class AlertScheduler {
    private final MailService mailService;
    private final OutboundRepository outboundRepository;

    // @Scheduled(cron = "second minute hour dayOfMonth month dayOfWeek")

    // @Scheduled(cron = "0 0 18 * * *")
    @Scheduled(cron = "0 */2 * * * *")
    public void alertOutboundsNearDueDate() {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);
        long page = 1;
        long limit = 20;
        while (true) {
            List<OutboundEntity> outbounds = this.outboundRepository.findUnconfirmedOutboundsInLastSevenDays(
                from, to, limit, (page - 1) * limit
            );
            outbounds.stream().forEach((e) -> {
                this.mailService.sendAsync(
                    e.getUser().getEmail(), 
                    "Alert outbound ", 
                    "Outbound %d must be export on %d/%d/%d".formatted(
                        e.getId(), 
                        e.getExpectedShippingDate().getDayOfMonth(),
                        e.getExpectedShippingDate().getMonth(),
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

    @Scheduled(cron = "0 */2 * * * *")
    public void alertLateOutbounds() {
        long page = 1;
        long limit = 20;
        while (true) {
            List<OutboundEntity> outbounds = this.outboundRepository.findLateOutbounds(limit, (page - 1) * limit);
            outbounds.stream().forEach((e) -> {
                this.mailService.sendAsync(
                    e.getUser().getEmail(), 
                    "Alert outbound ", 
                    "Outbound %d is not exported, due date on %d/%d/%d".formatted(
                        e.getId(), 
                        e.getExpectedShippingDate().getDayOfMonth(),
                        e.getExpectedShippingDate().getMonth(),
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