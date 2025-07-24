package com.training.warehouse.dto.response;

import java.time.LocalDateTime;

public interface RiskDelayedOutboundsProjection {
    LocalDateTime getExpectedShippingDate();
    String getUserEmail();
}
