package com.training.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.InboundEntity;

@Repository
public interface InboundRepository extends JpaRepository<InboundEntity, Long>{

}
