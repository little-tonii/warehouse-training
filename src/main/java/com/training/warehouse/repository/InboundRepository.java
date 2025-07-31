package com.training.warehouse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.InboundEntity;
import com.training.warehouse.repository.projection.InventoryProjection;

@Repository
public interface InboundRepository extends JpaRepository<InboundEntity, Long>{

    @Query(value = """
        SELECT 
        inbounds.id as id, 
        inbounds.created_at as createdAt, 
        inbounds.updated_at as updatedAt, 
        inbounds.invoice as invoice, 
        inbounds.product_type as productType, 
        inbounds.supplier_cd as supplierCd, 
        inbounds.receive_date as receiveDate,
        inbounds.quantity as quantity,
        COALESCE(inbounds.quantity - outboundsTemp.total_outbound, inbounds.quantity) AS inventory
        FROM inbounds
        LEFT JOIN (
            SELECT inb_id, SUM(quantity) AS total_outbound
            FROM outbounds
            GROUP BY inb_id
        ) outboundsTemp ON inbounds.id = outboundsTemp.inb_id
        WHERE COALESCE(inbounds.quantity - outboundsTemp.total_outbound, inbounds.quantity) > 0
        ORDER BY inbounds.created_at ASC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<InventoryProjection> findInventoryNative(long limit, long offset);

    @Query(value = """
        SELECT COUNT(*) FROM inbounds
        LEFT JOIN (
            SELECT inb_id, SUM(quantity) AS total_outbound
            FROM outbounds
            GROUP BY inb_id
        ) outboundsTemp ON inbounds.id = outboundsTemp.inb_id
        WHERE COALESCE(inbounds.quantity - outboundsTemp.total_outbound, inbounds.quantity) > 0
    """, nativeQuery = true)
    long countInventoryNative();
}
