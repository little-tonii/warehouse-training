package com.training.warehouse.repository;

import com.training.warehouse.dto.response.InboundSummaryMonthProjection;
import com.training.warehouse.dto.response.InboundSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.InboundEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface InboundRepository extends JpaRepository<InboundEntity, Long> {
    @Query(value = """
            SELECT new com.training.warehouse.dto.response.InboundSummaryResponse(
                i.productType,
                i.supplierCd,
                SUM(i.quantity)
            )
            FROM InboundEntity i
            GROUP BY i.productType, i.supplierCd
            """
            , countQuery = """
                SELECT COUNT(DISTINCT CONCAT(i.productType, '-', i.supplierCd))
                FROM InboundEntity i
            """)
    Page<InboundSummaryResponse> findInboundSummaryByProductTypeAndSupplierCd(Pageable pageable);

    @Query(value = """
            SELECT
                EXTRACT(MONTH FROM i.receive_date) AS month,
                i.product_type AS productType,
                i.supplier_cd AS supplierCd,
                SUM(i.quantity) AS totalQuantity
            FROM inbounds i
            WHERE
                EXTRACT(MONTH FROM i.receive_date) BETWEEN :startMonth AND :endMonth
                AND EXTRACT(YEAR FROM i.receive_date) = :year
            GROUP BY
                EXTRACT(MONTH FROM i.receive_date), i.product_type, i.supplier_cd
            ORDER BY month
            """, nativeQuery = true)
    List<InboundSummaryMonthProjection> findInbSummaryByMonth(@Param("startMonth") int startMonth, @Param("endMonth") int endMonth, @Param("year")int year);

    Optional<InboundEntity> findByInvoice(String invoice);
}
