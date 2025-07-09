package com.training.warehouse.repository;

import com.training.warehouse.dto.response.InboundSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.InboundEntity;

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
}
