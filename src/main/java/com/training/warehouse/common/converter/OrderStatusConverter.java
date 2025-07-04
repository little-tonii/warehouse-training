package com.training.warehouse.common.converter;

import com.training.warehouse.enumeric.OrderStatus;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Long> {

    @Override
    public Long convertToDatabaseColumn(OrderStatus status) {
        return status.getValue();
    }

    @Override
    public OrderStatus convertToEntityAttribute(Long value) {
        return OrderStatus.fromValue(value);   
    }
}