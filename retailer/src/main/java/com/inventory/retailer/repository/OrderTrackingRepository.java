package com.inventory.retailer.repository;

import com.inventory.retailer.entity.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {
    Optional<OrderTracking> findByOrderId(String orderId);
}
