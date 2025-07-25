package com.training.warehouse.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.training.warehouse.dto.response.RiskDelayedOutboundsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.OutboundEntity;

@Repository
public interface OutboundRepository extends JpaRepository<OutboundEntity, Long> {
    List<OutboundEntity> findByInboundId(long inboundId);
//    List<OutboundEntity> findByInboundIdAndShippingDateBetween(Long inboundId, LocalDateTime start, LocalDateTime end);
//    List<OutboundEntity> findByInboundIdAndShippingDateBefore(Long inboundId, LocalDateTime beforeDate);

    @Query(value = """
    SELECT  o.expected_shipping_date AS expectedShippingDate,
            u.email AS userEmail,
            o.inb_id AS inboundID
    FROM outbounds o
    JOIN users u ON u.id = o.user_id
    WHERE o.is_confirmed = false
      AND o.actual_shipping_date IS NULL
      AND o.expected_shipping_date = CURRENT_DATE
    """, nativeQuery = true)
    List<RiskDelayedOutboundsProjection> findAllRiskDelayedOutbounds();

//    @Query(value= """
//    """,nativeQuery = true)

}
