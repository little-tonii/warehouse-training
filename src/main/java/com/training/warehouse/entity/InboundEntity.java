package com.training.warehouse.entity;

import java.time.LocalDateTime;

import com.training.warehouse.common.OrderStatusConverter;
import com.training.warehouse.common.ProductTypeConverter;
import com.training.warehouse.common.SupplierCdConverter;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "inbounds")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InboundEntity extends BaseEntity {
    @Column(nullable = false, name = "invoice", length = 9)
    private String invoice;

    @Convert(converter = ProductTypeConverter.class)
    @Column(nullable = false, name = "product_type")
    private ProductType productType;

    @Convert(converter = SupplierCdConverter.class)
    @Column(nullable = false, name = "supplier_cd", length = 2)
    private SupplierCd supplierCd;

    @Column(nullable = true, name = "receive_date")
    private LocalDateTime receiveDate;

    @Convert(converter = OrderStatusConverter.class)
    @Column(nullable = false, name = "status")
    private OrderStatus status;

    @Column(nullable = true, name = "quantity")
    private long quantity;
}
