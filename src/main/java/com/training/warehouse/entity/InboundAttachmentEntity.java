package com.training.warehouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "inbound_attachment")
@Entity
public class InboundAttachmentEntity extends BaseEntity{

    @Column(nullable = false, name = "file_name")
    private String fileName;

    @Column(nullable = false, name = "file_type")
    private String filePath;

    @Column(nullable = false, name = "inb_id")
    private long inboundId;
    
}
