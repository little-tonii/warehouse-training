package com.training.warehouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "outbound_attachment")
public class OutboundAttachmentEntity extends BaseEntity{
    
    @Column(nullable = false, name = "file_name")
    private String fileName;

    @Column(nullable = false, name = "outb_id")
    private long outboundId;
}
