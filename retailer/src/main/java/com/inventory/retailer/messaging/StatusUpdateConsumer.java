package com.inventory.retailer.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.inventory.retailer.dto.OrderUpdateDTO;
import com.inventory.retailer.service.RetailerService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatusUpdateConsumer {

    private final RetailerService retailerService;

    public StatusUpdateConsumer(RetailerService retailerService) {
        this.retailerService = retailerService;
    }

    @RabbitListener(queues = "retailer.status.${retailer.id}")
    public void consumeStatusUpdate(OrderUpdateDTO update) {
        log.info("📢 Received status update for order {}: {}", update.getOrderId(), update.getStatus());
        
        try {
            if ("COMPLETED".equals(update.getStatus())) {
                retailerService.fulfillPurchase(update.getOrderId(), update.getPrice());
                log.info("✅ Order {} fulfillment triggered", update.getOrderId());
            } else {
                log.info("ℹ️ Order {} status updated to {}", update.getOrderId(), update.getStatus());
                // Optionally update tracking status for non-terminal states
            }
        } catch (Exception e) {
            log.error("❌ Failed to process status update for order {}: {}", update.getOrderId(), e.getMessage());
        }
    }
}
