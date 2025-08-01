package com.training.warehouse.repository.projection;

import java.time.LocalDateTime;

public interface UnconfirmedOutboundsInLastSevenDaysProjection {
    LocalDateTime getExpectedShippingDate();
    long getOutboundId();
    String getUserEmail();
}
