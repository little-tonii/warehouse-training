package com.training.warehouse.entity;

import java.time.LocalDateTime;

import com.training.warehouse.common.ShippingMethodConverter;
import com.training.warehouse.enumeric.ShippingMethod;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "outbounds")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OutboundEntity extends BaseEntity{

    @Column(nullable = false, name = "inb_id")
    private long inboundId;

    @Column(nullable = true, name = "quantity")
    private long quantity;

    @Convert(converter = ShippingMethodConverter.class)
    @Column(nullable = true, name = "shipping_method")
    private ShippingMethod shippingMethod;

    @Column(nullable = true, name = "shipping_date")
    private LocalDateTime shippingDate;
}
