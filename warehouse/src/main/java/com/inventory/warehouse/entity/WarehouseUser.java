package com.inventory.warehouse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouse_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // warehouse1

    @Column(nullable = false)
    private String password; // pass1
    
    private Long warehouseId; // 1
    private String warehouseName; //warehouse-1
}
