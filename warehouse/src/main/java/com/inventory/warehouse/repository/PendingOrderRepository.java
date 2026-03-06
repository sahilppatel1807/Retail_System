package com.inventory.warehouse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inventory.warehouse.entity.PendingOrder;

public interface PendingOrderRepository extends JpaRepository<PendingOrder, Long> {
    Optional<PendingOrder> findByOrderId(String orderId);
    List<PendingOrder> findByWarehouseId(Long warehouseId);
}
