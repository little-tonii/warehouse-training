package com.training.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.training.warehouse.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>{
}
