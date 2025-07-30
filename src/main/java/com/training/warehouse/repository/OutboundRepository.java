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
                i.product_type as productType,
                COALESCE(SUM(i.quantity), 0) - SUM(o.quantity) AS stockQuantity
            FROM inbounds i
            LEFT JOIN outbounds o ON o.inb_id = i.id
            GROUP BY i.product_type
            """,nativeQuery = true)
    List<StockProductType> findAllStockByProductType();
}
