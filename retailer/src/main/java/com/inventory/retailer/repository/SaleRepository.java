package com.inventory.retailer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inventory.retailer.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale,Long> {
    
}
