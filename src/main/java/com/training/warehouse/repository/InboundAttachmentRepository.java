package com.training.warehouse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.InboundAttachmentEntity;

@Repository
public interface InboundAttachmentRepository extends JpaRepository<InboundAttachmentEntity, Long>{
   List<InboundAttachmentEntity> findByInboundId(long inboundId);
}
