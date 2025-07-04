package com.training.warehouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RoleEntity extends BaseEntity{

    @Column(nullable = false, unique = true, name = "value")
    private int value;

    @Column(nullable = false, unique = true, name = "name")
    private String name;
}
