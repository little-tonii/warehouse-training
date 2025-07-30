package com.training.warehouse.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.training.warehouse.dto.response.StockProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.OutboundEntity;

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

    @Query(value = """
            SELECT
                COALESCE(o.product_type, i.product_type) AS productType,
                COALESCE(o.total_qty, 0) - COALESCE(i.total_qty, 0) AS stockQuantity
            FROM
                (SELECT product_type, SUM(quantity) AS total_qty
                 FROM outbounds
                 GROUP BY product_type) o
            FULL OUTER JOIN
                (SELECT product_type, SUM(quantity) AS total_qty
                 FROM inbounds
                 GROUP BY product_type) i
            ON o.product_type = i.product_type
            """,nativeQuery = true)
    List<StockProductType> findAllStockByProductType();
}
