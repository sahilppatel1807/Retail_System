package com.inventory.order_service.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "warehouses")
@Data
public class WarehouseConfig {

    private List<WarehouseInfo> list;

    @Data
    public static class WarehouseInfo {
        private Long id;
        private String host;
        private int port;

        public String getBaseUrl() {
            return "http://" + host + ":" + port;
        }
    }
}
