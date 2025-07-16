package com.training.warehouse.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.OutboundEntity;

@Repository
public interface OutboundRepository extends JpaRepository<OutboundEntity, Long>{
    List<OutboundEntity> findByInboundId(long inboundId);
//    List<OutboundEntity> findByInboundIdAndShippingDateBetween(Long inboundId, LocalDateTime start, LocalDateTime end);
//    List<OutboundEntity> findByInboundIdAndShippingDateBefore(Long inboundId, LocalDateTime beforeDate);
}
