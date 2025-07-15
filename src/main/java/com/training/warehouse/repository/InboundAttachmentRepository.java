package com.training.warehouse.repository;

import com.training.warehouse.entity.InboundAttachmentEntity;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InboundAttachmentRepository extends JpaRepository<InboundAttachmentEntity, Long>{
   List<InboundAttachmentEntity> findByInboundId(long inboundId);
}
