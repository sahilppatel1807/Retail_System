package com.inventory.retailer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inventory.retailer.entity.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase,Long>{
    
}
