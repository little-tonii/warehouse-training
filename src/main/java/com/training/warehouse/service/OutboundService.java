package com.training.warehouse.service;

import com.training.warehouse.entity.OutboundEntity;

import java.util.List;

public interface OutboundService {
    byte[] confirmOutboundById(long outboundId);

    void alertDelayedOutbounds();
}
