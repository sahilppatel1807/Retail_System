package com.inventory.retailer.repository;

import com.inventory.retailer.entity.RetailerInventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetailerInventoryHistoryRepository extends JpaRepository<RetailerInventoryHistory, Long> {
    
    // Find history for a specific product
    List<RetailerInventoryHistory> findByProductIdOrderByTransactionDateDesc(Long productId);
    
    // Find history for a retailer
    List<RetailerInventoryHistory> findByRetailerIdOrderByTransactionDateDesc(Long retailerId);
    
    // Find history by transaction type
    List<RetailerInventoryHistory> findByRetailerIdAndTransactionTypeOrderByTransactionDateDesc(
        Long retailerId, String transactionType
    );
}