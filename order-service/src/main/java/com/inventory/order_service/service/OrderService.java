package com.inventory.order_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.inventory.order_service.entity.InventoryItem;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService {

    private final InventoryService inventoryService;

    public OrderService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get inventory snapshot for debugging
     */
    public Map<Long, List<InventoryItem>> getInventorySnapshot() {
        return inventoryService.getInventorySnapshot();
    }
}