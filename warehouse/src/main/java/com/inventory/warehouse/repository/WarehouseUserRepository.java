package com.inventory.warehouse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inventory.warehouse.entity.WarehouseUser;

public interface WarehouseUserRepository extends JpaRepository<WarehouseUser, Long> {
    Optional<WarehouseUser> findByUsername(String username);
    Optional<WarehouseUser> findByUsernameAndPassword(String username, String password);
    
}
