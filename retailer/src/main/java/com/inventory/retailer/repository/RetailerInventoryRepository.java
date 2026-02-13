package com.inventory.retailer.repository;

import com.inventory.retailer.entity.RetailerInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerInventoryRepository extends JpaRepository<RetailerInventory, Long> {
    
    // Find inventory for a specific retailer and product
    Optional<RetailerInventory> findByRetailerIdAndProductId(Long retailerId, Long productId);
    
    // Find all inventory for a retailer
    List<RetailerInventory> findByRetailerId(Long retailerId);
}