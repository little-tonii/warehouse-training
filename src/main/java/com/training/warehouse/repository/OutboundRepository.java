package com.training.warehouse.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.training.warehouse.dto.response.RiskDelayedOutboundsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.dto.response.StockProjection;
@Repository
public interface OutboundRepository extends JpaRepository<OutboundEntity, Long> {
    List<OutboundEntity> findByInboundId(long inboundId);

    @Query("""
                SELECT outbound
                FROM OutboundEntity outbound
                WHERE (outbound.actualShippingDate IS NULL OR outbound.actualShippingDate > outbound.expectedShippingDate)
                  AND outbound.createdAt BETWEEN :from AND :to
            """)
    List<OutboundEntity> findLateOutboundsCreatedBetween(LocalDateTime from, LocalDateTime to);
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

    @Query(value= """ 
    SELECT
    COALESCE(ib_start.total_qty, 0) - COALESCE(ob_start.total_qty, 0) AS startQuantity,
    COALESCE(ib_end.total_qty, 0) - COALESCE(ob_end.total_qty, 0) AS endQuantity,
    (COALESCE(ib_end.total_qty, 0) - COALESCE(ob_end.total_qty, 0)) 
      - (COALESCE(ib_start.total_qty, 0) - COALESCE(ob_start.total_qty, 0)) AS diffQuantity
    FROM
    (SELECT SUM(quantity) AS total_qty
     FROM inbounds
     WHERE receive_date < DATE_TRUNC('month', MAKE_DATE(:year, :month, 1))
    ) ib_start,

    (SELECT SUM(quantity) AS total_qty
     FROM outbounds 
     WHERE actual_shipping_date < DATE_TRUNC('month', MAKE_DATE(:year, :month, 1))
    ) ob_start,

    (SELECT SUM(quantity) AS total_qty
     FROM inbounds
     WHERE receive_date < (DATE_TRUNC('month', MAKE_DATE(:year, :month, 1)) + INTERVAL '1 month')
    ) ib_end,

    (SELECT SUM(quantity) AS total_qty
     FROM outbounds
     WHERE actual_shipping_date < (DATE_TRUNC('month', MAKE_DATE(:year, :month, 1)) + INTERVAL '1 month')
    ) ob_end
    """,nativeQuery = true)
    StockProjection findAllDiffQuantity(@Param("month") int month, @Param("year") int year);

}
