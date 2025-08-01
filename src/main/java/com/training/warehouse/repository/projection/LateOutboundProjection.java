package com.training.warehouse.repository.projection;

import java.time.LocalDateTime;

public interface LateOutboundProjection {
    LocalDateTime getExpectedShippingDate();
    long getOutboundId();
    String getUserEmail();
}
