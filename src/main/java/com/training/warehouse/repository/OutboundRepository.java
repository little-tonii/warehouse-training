package com.training.warehouse.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    Optional<OutboundEntity> findFirstByInboundId(long inboundId);
}
