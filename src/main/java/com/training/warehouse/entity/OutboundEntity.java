package com.training.warehouse.entity;

import java.time.LocalDateTime;

import com.training.warehouse.common.converter.ShippingMethodConverter;
import com.training.warehouse.enumeric.ShippingMethod;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

    @Column(nullable = false, name = "quantity")
    private long quantity;

    @Convert(converter = ShippingMethodConverter.class)
    @Column(nullable = false, name = "shipping_method")
    private ShippingMethod shippingMethod;

    @Column(nullable = false, name = "expected_shipping_date")
    private LocalDateTime expectedShippingDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private UserEntity user;

    @Column(nullable = false, name = "is_confirmed")
    @Builder.Default
    private boolean isConfirmed = false;

    @Column(name = "actual_shipping_date", nullable = true)
    private LocalDateTime actualShippingDate;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "attachment_id", referencedColumnName = "id", nullable = true)
    private OutboundAttachmentEntity attachment;
}
