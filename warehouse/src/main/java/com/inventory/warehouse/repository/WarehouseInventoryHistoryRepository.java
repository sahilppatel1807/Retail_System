package com.inventory.warehouse.repository;

import com.inventory.warehouse.entity.WarehouseInventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseInventoryHistoryRepository extends JpaRepository<WarehouseInventoryHistory, Long> {
    
    // Find all history for a specific product
    List<WarehouseInventoryHistory> findByProductIdOrderByTransactionDateDesc(Long productId);
    
    // Find all history for a warehouse
    List<WarehouseInventoryHistory> findByWarehouseIdOrderByTransactionDateDesc(Long warehouseId);
    
    // Find all sales (SOLD transactions)
    List<WarehouseInventoryHistory> findByTransactionTypeOrderByTransactionDateDesc(String transactionType);
    
    // Find history by product and warehouse
    List<WarehouseInventoryHistory> findByProductIdAndWarehouseIdOrderByTransactionDateDesc(
        Long productId, Long warehouseId
    );
}