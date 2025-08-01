package com.training.warehouse.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.OutboundEntity;
import com.training.warehouse.repository.projection.LateOutboundProjection;
import com.training.warehouse.repository.projection.UnconfirmedOutboundsInLastSevenDaysProjection;

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

    @Query(value="""
      SELECT
        outbounds.expected_shipping_date as expectedShippingDate,
        outbounds.id as outboundId,
        users.email as userEmail
      FROM outbounds
      INNER JOIN users ON users.id = outbounds.user_id
      WHERE is_confirmed = false AND expected_shipping_date BETWEEN :from AND :to
      ORDER BY expected_shipping_date ASC
      LIMIT :limit OFFSET :offset    
    """, nativeQuery = true)
    List<UnconfirmedOutboundsInLastSevenDaysProjection> findUnconfirmedOutboundsInLastSevenDaysNative(LocalDateTime from, LocalDateTime to, long limit, long offset);

    @Query(value = """
      SELECT
        outbounds.expected_shipping_date as expectedShippingDate,
        outbounds.id as outboundId,
        users.email as userEmail
      FROM outbounds
      INNER JOIN users ON users.id = outbounds.user_id
      WHERE is_confirmed = false AND expected_shipping_date < NOW()
      ORDER BY expected_shipping_date ASC
      LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<LateOutboundProjection> findLateOutboundsNative(long limit, long offset);
}
