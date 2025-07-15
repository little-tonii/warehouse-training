package com.training.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.OutboundAttachmentEntity;

@Repository
public interface OutboundAttachmentRepository extends JpaRepository<OutboundAttachmentEntity, Long>{
}
