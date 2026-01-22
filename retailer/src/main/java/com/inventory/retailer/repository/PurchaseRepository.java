package com.inventory.retailer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inventory.retailer.entity.Purchase;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase,Long>{
    Optional<Purchase> findByProductName(String productName);
}
