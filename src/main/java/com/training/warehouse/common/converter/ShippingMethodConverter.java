package com.training.warehouse.common.converter;

import com.training.warehouse.enumeric.ShippingMethod;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShippingMethodConverter  implements AttributeConverter<ShippingMethod, String> {
     @Override
    public String convertToDatabaseColumn(ShippingMethod shippingMethod) {
        return shippingMethod != null ? shippingMethod.getCode() : null;
    }

    @Override
    public ShippingMethod convertToEntityAttribute(String shippingMethod) {
        if (shippingMethod == null) {
            return null;
        }
        return ShippingMethod.fromCode(shippingMethod);
    }
}
