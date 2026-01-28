package com.inventory.retailer.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "warehouse")
@Data
public class WarehouseConfig {
    
    private Map<String, WarehouseInfo> config = new HashMap<>();
    
    @Data
    public static class WarehouseInfo {
        private String host;
        private int port;
    }
    
    // Get warehouse info by ID
    public WarehouseInfo getWarehouse(Long warehouseId) {
        WarehouseInfo info = config.get(warehouseId.toString());
        if (info == null) {
            throw new RuntimeException("Warehouse " + warehouseId + " not found in configuration");
        }
        return info;
    }
}
