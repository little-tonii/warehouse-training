package com.training.warehouse.common;

import com.training.warehouse.enumeric.SupplierCd;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true) 
public class SupplierCdConverter implements AttributeConverter<SupplierCd, String> {

    @Override
    public String convertToDatabaseColumn(SupplierCd supplerCd) {
        return supplerCd.getCode();
    }

    @Override
    public SupplierCd convertToEntityAttribute(String supplierCd) {
        return SupplierCd.fromCode(supplierCd);
    }
}
