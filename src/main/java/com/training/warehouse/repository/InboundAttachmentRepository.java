package com.training.warehouse.repository;

import com.training.warehouse.entity.InboundAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundAttachmentRepository extends JpaRepository<InboundAttachmentEntity,Long> {
}
