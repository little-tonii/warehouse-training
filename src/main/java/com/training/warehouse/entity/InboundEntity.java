package com.training.warehouse.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.training.warehouse.common.converter.OrderStatusConverter;
import com.training.warehouse.common.converter.ProductTypeConverter;
import com.training.warehouse.common.converter.SupplierCdConverter;
import com.training.warehouse.enumeric.OrderStatus;
import com.training.warehouse.enumeric.ProductType;
import com.training.warehouse.enumeric.SupplierCd;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity user;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "inb_id", referencedColumnName = "id")
    private List<OutboundEntity> outbounds;
}
