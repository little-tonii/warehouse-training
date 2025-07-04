package com.training.warehouse.common;

import com.training.warehouse.enumeric.ProductType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductTypeConverter implements AttributeConverter<ProductType, String> {

    @Override
    public String convertToDatabaseColumn(ProductType productType) {
        return productType.getName();
    }

    @Override
    public ProductType convertToEntityAttribute(String productType) {
        return ProductType.fromString(productType);
    }
}
