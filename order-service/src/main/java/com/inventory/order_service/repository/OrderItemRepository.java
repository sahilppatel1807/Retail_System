package com.inventory.order_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inventory.order_service.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find order by order ID
    Optional<OrderItem> findByOrderId(String orderId);
    
    // Find all orders by status
    List<OrderItem> findByStatus(String status);
    
    // Find all orders for a retailer
    List<OrderItem> findByRetailerId(Long retailerId);
    
    // Find all orders assigned to a warehouse
    List<OrderItem> findByWarehouseId(Long warehouseId);
    
    // Find orders by retailer and status
    List<OrderItem> findByRetailerIdAndStatus(Long retailerId, String status);
    
}
