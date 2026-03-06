# Warehouse & Order-Service — Complete Code Reference

---

# ═══════════════════════════════════════════
# 1. WAREHOUSE SERVICE
# ═══════════════════════════════════════════

## Dockerfile
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>
    <groupId>com.inventory</groupId>
    <artifactId>warehouse</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>warehouse</name>
    <description>warehouse to keep the items</description>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>3.3.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## src/main/resources/application.properties
```properties
spring.application.name=warehouse

server.port=${SERVER_PORT:8081}
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/retail_system}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:sahil}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.default_schema=warehouse_schema

# RabbitMQ Configuration
spring.rabbitmq.host=${SPRING_RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${SPRING_RABBITMQ_PORT:5672}
spring.rabbitmq.username=${SPRING_RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${SPRING_RABBITMQ_PASSWORD:guest}
```

---

## WarehouseApplication.java
```java
package com.inventory.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
```

---

## config/RabbitMQConfig.java
```java
package com.inventory.warehouse.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "warehouse.exchange";

    public static final String STATUS_UPDATE_EXCHANGE = "status.update.exchange";
    public static final String STATUS_UPDATE_ROUTING_KEY = "status.update";

    @Bean
    public TopicExchange warehouseExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange statusUpdateExchange() {
        return new TopicExchange(STATUS_UPDATE_EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
```

---

## controller/ItemController.java
```java
package com.inventory.warehouse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.inventory.warehouse.entity.Item;
import com.inventory.warehouse.entity.WarehouseInventoryHistory;
import com.inventory.warehouse.service.ItemService;

@RestController
@RequestMapping("/api/warehouse")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public Item create(@RequestBody Item item) {
        return service.createItem(item);
    }

    @GetMapping("/all")
    public List<Item> getAllItem() {
        return service.getAllItems();
    }

    @GetMapping("/{id}")
    public Item getItem(@PathVariable Long id) {
        return service.findItem(id);
    }

    @PostMapping("/buy")
    public Item buyItem(@RequestParam Long retailerId,
                        @RequestParam Long itemId,
                        @RequestParam int quantity) {
        return service.sellItem(retailerId, itemId, quantity);
    }

    @PutMapping("/{id}")
    public Item updateItem(@PathVariable Long id, @RequestBody Item item) {
        return service.updateItem(id, item);
    }

    @GetMapping("/history/product/{productId}")
    public List<WarehouseInventoryHistory> getProductHistory(@PathVariable Long productId) {
        return service.getProductHistory(productId);
    }

    @GetMapping("/history")
    public List<WarehouseInventoryHistory> getWarehouseHistory() {
        return service.getWarehouseHistory();
    }
}
```

---

## dto/OrderUpdateDTO.java
```java
package com.inventory.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {
    private String orderId;
    private String status;
    private String message;
    private float price;
}
```

## dto/StockUpdateMessage.java
```java
package com.inventory.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateMessage {
    private Long warehouseId;
    private Long productId;
    private String productName;
    private int newStock;
    private float price;
}
```

---

## entity/Item.java
```java
package com.inventory.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long warehouseId;
    private String productName;
    private float price;
    private int stockOnHand;
}
```

## entity/WarehouseInventoryHistory.java
```java
package com.inventory.warehouse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_inventory_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseInventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long warehouseId;
    private Long productId;
    private String productName;

    @Column(nullable = false)
    private String transactionType;  // ADDED, SOLD, ADJUSTED

    private int quantity;
    private float priceAtTransaction;

    private int stockBefore;
    private int stockAfter;

    private Long retailerId;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    private String notes;
}
```

---

## repository/ItemRepository.java
```java
package com.inventory.warehouse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inventory.warehouse.entity.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByProductName(String productName);
}
```

## repository/WarehouseInventoryHistoryRepository.java
```java
package com.inventory.warehouse.repository;

import com.inventory.warehouse.entity.WarehouseInventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseInventoryHistoryRepository extends JpaRepository<WarehouseInventoryHistory, Long> {

    List<WarehouseInventoryHistory> findByProductIdOrderByTransactionDateDesc(Long productId);

    List<WarehouseInventoryHistory> findByWarehouseIdOrderByTransactionDateDesc(Long warehouseId);

    List<WarehouseInventoryHistory> findByTransactionTypeOrderByTransactionDateDesc(String transactionType);

    List<WarehouseInventoryHistory> findByProductIdAndWarehouseIdOrderByTransactionDateDesc(
        Long productId, Long warehouseId
    );
}
```

---

## messaging/OrderConsumer.java
```java
package com.inventory.warehouse.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inventory.warehouse.dto.OrderUpdateDTO;
import com.inventory.warehouse.service.ItemService;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Service
@Slf4j
public class OrderConsumer {

    private final ItemService itemService;
    private final StatusUpdateProducer statusUpdateProducer;

    @Value("${warehouse.id:1}")
    private Long warehouseId;

    public OrderConsumer(ItemService itemService, StatusUpdateProducer statusUpdateProducer) {
        this.itemService = itemService;
        this.statusUpdateProducer = statusUpdateProducer;
    }

    @RabbitListener(queues = "order.routed.warehouse.${warehouse.id:1}")
    public void consumeOrder(Map<String, Object> orderData) {
        String orderId = (String) orderData.get("orderId");
        Long productId = Long.valueOf(orderData.get("productId").toString());
        int quantity = (int) orderData.get("quantity");
        Long retailerId = Long.valueOf(orderData.get("retailerId").toString());

        log.info("📦 Warehouse {} received order {} for product {}", warehouseId, orderId, productId);

        try {
            com.inventory.warehouse.entity.Item item = itemService.sellItem(retailerId, productId, quantity);
            statusUpdateProducer.sendStatusUpdate(new OrderUpdateDTO(
                orderId, "COMPLETED",
                "Order processed successfully by warehouse " + warehouseId,
                item.getPrice()
            ));
            log.info("✅ Order {} completed by warehouse {}", orderId, warehouseId);
        } catch (Exception e) {
            log.error("❌ Failed to process order {}: {}", orderId, e.getMessage());
            statusUpdateProducer.sendStatusUpdate(new OrderUpdateDTO(orderId, "FAILED", e.getMessage(), 0.0f));
        }
    }
}
```

## messaging/StatusUpdateProducer.java
```java
package com.inventory.warehouse.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.inventory.warehouse.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatusUpdateProducer {
    private final RabbitTemplate rabbitTemplate;

    public StatusUpdateProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendStatusUpdate(Object orderStatusUpdate) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.STATUS_UPDATE_EXCHANGE,
            RabbitMQConfig.STATUS_UPDATE_ROUTING_KEY,
            orderStatusUpdate
        );
        log.info("📨 Sent status update for order to exchange: {}", RabbitMQConfig.STATUS_UPDATE_EXCHANGE);
    }
}
```

## messaging/StockUpdateProducer.java
```java
package com.inventory.warehouse.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.inventory.warehouse.dto.StockUpdateMessage;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StockUpdateProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${warehouse.id}")
    private Long warehouseId;

    private static final String EXCHANGE = "warehouse.exchange";
    private static final String ROUTING_KEY = "warehouse.stock.update";

    public StockUpdateProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendStockUpdate(Long productId, String productName, int newStock, float price) {
        StockUpdateMessage message = new StockUpdateMessage(warehouseId, productId, productName, newStock, price);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
        log.info("📤 [Warehouse-{}] Sent stock update: Product {} now has {} units", warehouseId, productId, newStock);
    }
}
```

---

## service/ItemService.java
```java
package com.inventory.warehouse.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inventory.warehouse.entity.Item;
import com.inventory.warehouse.entity.WarehouseInventoryHistory;
import com.inventory.warehouse.messaging.StockUpdateProducer;
import com.inventory.warehouse.repository.ItemRepository;
import com.inventory.warehouse.repository.WarehouseInventoryHistoryRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ItemService {
    private final ItemRepository repository;
    private final WarehouseInventoryHistoryRepository historyRepository;
    private final StockUpdateProducer stockUpdateProducer;

    @Value("${warehouse.id:1}")
    private Long warehouseId;

    public ItemService(ItemRepository repository,
                       WarehouseInventoryHistoryRepository historyRepository,
                       StockUpdateProducer stockUpdateProducer) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.stockUpdateProducer = stockUpdateProducer;
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void broadcastStockOnStartup() {
        log.info("🚀 System ready. Broadcasting initial stock for Warehouse {}...", warehouseId);
        List<Item> items = repository.findAll();
        for (Item item : items) {
            stockUpdateProducer.sendStockUpdate(item.getId(), item.getProductName(), item.getStockOnHand(), item.getPrice());
        }
        log.info("✅ Broadcasted {} items.", items.size());
    }

    @Transactional
    public Item createItem(Item item) {
        item.setWarehouseId(warehouseId);

        return repository.findByProductName(item.getProductName())
                .map(existingItem -> {
                    int stockBefore = existingItem.getStockOnHand();
                    int quantityAdded = item.getStockOnHand();
                    int stockAfter = stockBefore + quantityAdded;

                    existingItem.setStockOnHand(stockAfter);
                    existingItem.setPrice(item.getPrice());
                    Item saved = repository.save(existingItem);

                    recordHistory(saved.getId(), saved.getProductName(), "ADDED",
                        quantityAdded, saved.getPrice(), stockBefore, stockAfter, null, "Restocked inventory");

                    stockUpdateProducer.sendStockUpdate(saved.getId(), saved.getProductName(), saved.getStockOnHand(), saved.getPrice());
                    log.info("✅ Added {} units. Stock: {} → {}", quantityAdded, stockBefore, stockAfter);
                    return saved;
                })
                .orElseGet(() -> {
                    Item newItem = new Item();
                    newItem.setProductName(item.getProductName());
                    newItem.setPrice(item.getPrice());
                    newItem.setStockOnHand(item.getStockOnHand());
                    newItem.setWarehouseId(warehouseId);
                    Item saved = repository.save(newItem);

                    recordHistory(saved.getId(), saved.getProductName(), "ADDED",
                        saved.getStockOnHand(), saved.getPrice(), 0, saved.getStockOnHand(), null, "Initial stock");

                    stockUpdateProducer.sendStockUpdate(saved.getId(), saved.getProductName(), saved.getStockOnHand(), saved.getPrice());
                    log.info("✅ Created new product with {} units", saved.getStockOnHand());
                    return saved;
                });
    }

    public Item findItem(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ENTITY NOT FOUND WITH THE " + id));
    }

    public List<Item> getAllItems() {
        return repository.findAll();
    }

    @Transactional
    public Item sellItem(Long retailerId, Long itemId, int quantity) {
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getStockOnHand() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        int stockBefore = item.getStockOnHand();
        int stockAfter = stockBefore - quantity;

        item.setStockOnHand(stockAfter);
        Item saved = repository.save(item);

        recordHistory(saved.getId(), saved.getProductName(), "SOLD",
            quantity, saved.getPrice(), stockBefore, stockAfter, retailerId, "Sold to Retailer " + retailerId);

        stockUpdateProducer.sendStockUpdate(saved.getId(), saved.getProductName(), saved.getStockOnHand(), saved.getPrice());
        log.info("✅ [Warehouse-{}] Sold {} units of {}. Stock: {} → {}", warehouseId, quantity, saved.getProductName(), stockBefore, stockAfter);
        return saved;
    }

    @Transactional
    public Item updateItem(Long id, Item updatedItem) {
        Item existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));

        int stockBefore = existing.getStockOnHand();
        int stockAfter = updatedItem.getStockOnHand();
        int difference = stockAfter - stockBefore;

        existing.setProductName(updatedItem.getProductName());
        existing.setPrice(updatedItem.getPrice());
        existing.setStockOnHand(stockAfter);

        Item saved = repository.save(existing);

        recordHistory(saved.getId(), saved.getProductName(), "ADJUSTED",
            Math.abs(difference), saved.getPrice(), stockBefore, stockAfter, null,
            difference > 0 ? "Stock increased" : "Stock decreased");

        stockUpdateProducer.sendStockUpdate(saved.getId(), saved.getProductName(), saved.getStockOnHand(), saved.getPrice());
        log.info("✅ Updated product. Stock: {} → {}", stockBefore, stockAfter);
        return saved;
    }

    public List<WarehouseInventoryHistory> getProductHistory(Long productId) {
        return historyRepository.findByProductIdOrderByTransactionDateDesc(productId);
    }

    public List<WarehouseInventoryHistory> getWarehouseHistory() {
        return historyRepository.findByWarehouseIdOrderByTransactionDateDesc(warehouseId);
    }

    private void recordHistory(Long productId, String productName, String transactionType,
                                int quantity, float price, int stockBefore, int stockAfter,
                                Long retailerId, String notes) {
        WarehouseInventoryHistory history = new WarehouseInventoryHistory();
        history.setWarehouseId(warehouseId);
        history.setProductId(productId);
        history.setProductName(productName);
        history.setTransactionType(transactionType);
        history.setQuantity(quantity);
        history.setPriceAtTransaction(price);
        history.setStockBefore(stockBefore);
        history.setStockAfter(stockAfter);
        history.setRetailerId(retailerId);
        history.setTransactionDate(LocalDateTime.now());
        history.setNotes(notes);
        historyRepository.save(history);
    }
}
```

---
---

# ═══════════════════════════════════════════
# 2. ORDER-SERVICE
# ═══════════════════════════════════════════

## Dockerfile
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.1</version>
        <relativePath/>
    </parent>
    <groupId>com.inventory</groupId>
    <artifactId>order-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>order-service</name>
    <description>order service having all of the combine data of all warehouses</description>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## src/main/resources/application.properties
```properties
spring.application.name=order-service
server.port=8084

# Warehouse Configuration
warehouses.list[0].id=1
warehouses.list[0].host=warehouse1
warehouses.list[0].port=8081

warehouses.list[1].id=2
warehouses.list[1].host=warehouse2
warehouses.list[1].port=8091

warehouses.list[2].id=3
warehouses.list[2].host=warehouse3
warehouses.list[2].port=8101

# RabbitMQ Configuration
spring.rabbitmq.host=${SPRING_RABBITMQ_HOST:localhost}
spring.rabbitmq.port=${SPRING_RABBITMQ_PORT:5672}
spring.rabbitmq.username=${SPRING_RABBITMQ_USERNAME:guest}
spring.rabbitmq.password=${SPRING_RABBITMQ_PASSWORD:guest}

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/retail_system}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:sahil}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:root}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.default_schema=order_service_schema
```

---

## OrderServiceApplication.java
```java
package com.inventory.order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

---

## config/RabbitMQConfig.java
```java
package com.inventory.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // stock update
    public static final String QUEUE_NAME = "stock.updates.queue";
    public static final String EXCHANGE_NAME = "warehouse.exchange";
    public static final String ROUTING_KEY = "warehouse.stock.update";

    // order accepted
    public static final String ORDER_ACCEPTED_QUEUE = "order.accepted.queue";
    public static final String ORDER_ACCEPTED_EXCHANGE = "order.accepted.exchange";
    public static final String ORDER_ACCEPTED_ROUTING_KEY = "order.accepted";

    // order routed
    public static final String ORDER_ROUTED_EXCHANGE = "order.routed.exchange";
    public static final String ORDER_ROUTED_ROUTING_KEY_WAREHOUSE1 = "warehouse.1.routed";
    public static final String ORDER_ROUTED_ROUTING_KEY_WAREHOUSE2 = "warehouse.2.routed";
    public static final String ORDER_ROUTED_ROUTING_KEY_WAREHOUSE3 = "warehouse.3.routed";
    public static final String WAREHOUSE_1_QUEUE = "order.routed.warehouse.1";
    public static final String WAREHOUSE_2_QUEUE = "order.routed.warehouse.2";
    public static final String WAREHOUSE_3_QUEUE = "order.routed.warehouse.3";

    // status update
    public static final String STATUS_UPDATE_EXCHANGE = "status.update.exchange";
    public static final String STATUS_UPDATE_ROUTING_KEY = "status.update";
    public static final String STATUS_UPDATE_QUEUE = "status.update.queue";

    // retailer notification
    public static final String RETAILER_STATUS_EXCHANGE = "retailer.status.exchange";

    @Bean public Queue stockUpdatesQueue() { return new Queue(QUEUE_NAME, true); }
    @Bean public TopicExchange warehouseExchange() { return new TopicExchange(EXCHANGE_NAME); }
    @Bean public Binding binding(Queue stockUpdatesQueue, TopicExchange warehouseExchange) {
        return BindingBuilder.bind(stockUpdatesQueue).to(warehouseExchange).with(ROUTING_KEY);
    }

    @Bean public Queue orderAcceptedQueue() { return new Queue(ORDER_ACCEPTED_QUEUE, true); }
    @Bean public TopicExchange orderAcceptedExchange() { return new TopicExchange(ORDER_ACCEPTED_EXCHANGE); }
    @Bean public Binding orderAcceptedBinding(Queue orderAcceptedQueue, TopicExchange orderAcceptedExchange) {
        return BindingBuilder.bind(orderAcceptedQueue).to(orderAcceptedExchange).with(ORDER_ACCEPTED_ROUTING_KEY);
    }

    @Bean public TopicExchange orderExchange() { return new TopicExchange(ORDER_ROUTED_EXCHANGE); }
    @Bean public Queue warehouse1Queue() { return new Queue(WAREHOUSE_1_QUEUE, true); }
    @Bean public Queue warehouse2Queue() { return new Queue(WAREHOUSE_2_QUEUE, true); }
    @Bean public Queue warehouse3Queue() { return new Queue(WAREHOUSE_3_QUEUE, true); }
    @Bean public Binding binding1(Queue warehouse1Queue, TopicExchange orderExchange) {
        return BindingBuilder.bind(warehouse1Queue).to(orderExchange).with(ORDER_ROUTED_ROUTING_KEY_WAREHOUSE1);
    }
    @Bean public Binding binding2(Queue warehouse2Queue, TopicExchange orderExchange) {
        return BindingBuilder.bind(warehouse2Queue).to(orderExchange).with(ORDER_ROUTED_ROUTING_KEY_WAREHOUSE2);
    }
    @Bean public Binding binding3(Queue warehouse3Queue, TopicExchange orderExchange) {
        return BindingBuilder.bind(warehouse3Queue).to(orderExchange).with(ORDER_ROUTED_ROUTING_KEY_WAREHOUSE3);
    }

    @Bean public Queue statusUpdateQueue() { return new Queue(STATUS_UPDATE_QUEUE, true); }
    @Bean public TopicExchange statusUpdateExchange() { return new TopicExchange(STATUS_UPDATE_EXCHANGE); }
    @Bean public Binding statusUpdateBinding(Queue statusUpdateQueue, TopicExchange statusUpdateExchange) {
        return BindingBuilder.bind(statusUpdateQueue).to(statusUpdateExchange).with(STATUS_UPDATE_ROUTING_KEY);
    }

    @Bean public TopicExchange retailerStatusExchange() { return new TopicExchange(RETAILER_STATUS_EXCHANGE); }

    @Bean public MessageConverter jsonMessageConverter() { return new Jackson2JsonMessageConverter(); }
}
```

## config/WarehouseConfig.java
```java
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
```

---

## controller/OrderController.java
```java
package com.inventory.order_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.inventory.order_service.dto.PurchaseRequest;
import com.inventory.order_service.entity.InventoryItem;
import com.inventory.order_service.entity.OrderItem;
import com.inventory.order_service.service.OrderService;
import com.inventory.order_service.service.OrderItemService;
import com.inventory.order_service.service.OrderProducer;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/order-service")
@Slf4j
public class OrderController {

    private final OrderService service;
    private final OrderItemService orderItemService;
    private final OrderProducer orderProducer;

    public OrderController(OrderService service, OrderItemService orderItemService, OrderProducer orderProducer) {
        this.service = service;
        this.orderItemService = orderItemService;
        this.orderProducer = orderProducer;
    }

    @PostMapping("/purchase")
    public ResponseEntity<OrderItem> purchase(@Valid @RequestBody PurchaseRequest request) {
        log.info("📨 Received purchase request: productId={}, qty={}, retailerId={}",
            request.getProductId(), request.getQuantity(), request.getRetailerId());

        OrderItem orderItem = orderItemService.createOrder(
            request.getRetailerId(), request.getProductId(), null, request.getQuantity(), null
        );

        log.info("✅ Order {} accepted and sent for routing", orderItem.getOrderId());
        return ResponseEntity.ok(orderItem);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running");
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<Long, List<InventoryItem>>> getInventory() {
        return ResponseEntity.ok(service.getInventorySnapshot());
    }

    @PostMapping("/test-order")
    public ResponseEntity<OrderItem> createTestOrder() {
        OrderItem orderItem = orderItemService.createOrder(1L, 5L, "Laptop", 10, 1L);
        return ResponseEntity.ok(orderItem);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderItem>> getAllOrders() {
        return ResponseEntity.ok(orderItemService.getAllOrders());
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderItem> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderItemService.getOrder(orderId));
    }

    @PostMapping
    public String sendOrder(@RequestBody String order) {
        orderProducer.sendOrderMessage(order);
        return "Order sent to RabbitMQ!";
    }
}
```

---

## dto/ItemResponse.java
```java
package com.inventory.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private Long id;
    private String productName;
    private float price;
    private int stockOnHand;
    private Long warehouseId;
}
```

## dto/OrderUpdateDTO.java
```java
package com.inventory.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {
    private String orderId;
    private String status;
    private String message;
    private float price;
}
```

## dto/PurchaseRequest.java
```java
package com.inventory.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotNull(message = "Retailer ID is required")
    private Long retailerId;
}
```

## dto/StockUpdateMessage.java
```java
package com.inventory.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateMessage {
    private Long warehouseId;
    private Long productId;
    private String productName;
    private int newStock;
    private float price;
}
```

---

## entity/InventoryItem.java
```java
package com.inventory.order_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
    private Long productId;
    private String productName;
    private Long warehouseId;
    private int stockOnHand;
    private float price;
}
```

## entity/OrderItem.java
```java
package com.inventory.order_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderId;        // ORD-2025-02-15-12345

    private String referenceId;    // REF-67890

    private Long retailerId;
    private Long productId;
    private String productName;
    private int quantity;

    private Long warehouseId;

    @Column(nullable = false)
    private String status;         // ACCEPTED, ROUTED, PROCESSING, COMPLETED, FAILED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private float price;

    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    private String notes;
}
```

---

## repository/OrderItemRepository.java
```java
package com.inventory.order_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inventory.order_service.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Optional<OrderItem> findByOrderId(String orderId);
    List<OrderItem> findByStatus(String status);
    List<OrderItem> findByRetailerId(Long retailerId);
    List<OrderItem> findByWarehouseId(Long warehouseId);
    List<OrderItem> findByRetailerIdAndStatus(Long retailerId, String status);
}
```

---

## messaging/StatusUpdateConsumer.java
```java
package com.inventory.order_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.inventory.order_service.config.RabbitMQConfig;
import com.inventory.order_service.dto.OrderUpdateDTO;
import com.inventory.order_service.entity.OrderItem;
import com.inventory.order_service.service.OrderItemService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StatusUpdateConsumer {

    private final OrderItemService orderItemService;
    private final RabbitTemplate rabbitTemplate;

    public StatusUpdateConsumer(OrderItemService orderItemService, RabbitTemplate rabbitTemplate) {
        this.orderItemService = orderItemService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.STATUS_UPDATE_QUEUE)
    public void consumeStatusUpdate(OrderUpdateDTO update) {
        log.info("📢 Received status update for order {}: {} - {}", update.getOrderId(), update.getStatus(), update.getMessage());

        try {
            OrderItem orderItem = orderItemService.updateOrderStatus(update.getOrderId(), update.getStatus());
            orderItemService.addNotes(update.getOrderId(), update.getMessage());
            log.info("✅ Database updated for order {}", update.getOrderId());

            update.setPrice(orderItem.getPrice());

            String routingKey = "retailer." + orderItem.getRetailerId();
            rabbitTemplate.convertAndSend(RabbitMQConfig.RETAILER_STATUS_EXCHANGE, routingKey, update);
            log.info("📤 Status update forwarded to {} with routing key {}", RabbitMQConfig.RETAILER_STATUS_EXCHANGE, routingKey);

        } catch (Exception e) {
            log.error("❌ Failed to update status for order {}: {}", update.getOrderId(), e.getMessage());
        }
    }
}
```

## messaging/StockUpdateConsumer.java
```java
package com.inventory.order_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.inventory.order_service.dto.StockUpdateMessage;
import com.inventory.order_service.service.InventoryService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StockUpdateConsumer {

    private final InventoryService inventoryService;

    public StockUpdateConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @RabbitListener(queues = "stock.updates.queue")
    public void handleStockUpdate(StockUpdateMessage message) {
        log.info("📥 Received stock update from Warehouse {}: Product {} now has {} units",
            message.getWarehouseId(), message.getProductId(), message.getNewStock());

        inventoryService.updateProductStock(
            message.getProductId(), message.getProductName(),
            message.getWarehouseId(), message.getNewStock(), message.getPrice()
        );

        log.info("✅ Cache updated successfully");
    }
}
```

---

## service/InventoryService.java
```java
package com.inventory.order_service.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.inventory.order_service.config.WarehouseConfig;
import com.inventory.order_service.config.WarehouseConfig.WarehouseInfo;
import com.inventory.order_service.entity.InventoryItem;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InventoryService {

    private final Map<Long, List<InventoryItem>> inventoryCache = new ConcurrentHashMap<>();
    private final WarehouseConfig warehouseConfig;

    public InventoryService(WarehouseConfig warehouseConfig) {
        this.warehouseConfig = warehouseConfig;
    }

    public void updateProductStock(Long productId, String productName, Long warehouseId, int newStock, float price) {
        List<InventoryItem> warehouseList = inventoryCache.computeIfAbsent(productId, k -> new ArrayList<>());

        InventoryItem existing = warehouseList.stream()
                .filter(item -> item.getWarehouseId().equals(warehouseId))
                .findFirst().orElse(null);

        if (existing != null) {
            existing.setStockOnHand(newStock);
            existing.setPrice(price);
            existing.setProductName(productName);
            log.info("📝 Updated cache: Product {} in Warehouse {} → {} units", productId, warehouseId, newStock);
        } else {
            warehouseList.add(new InventoryItem(productId, productName, warehouseId, newStock, price));
            log.info("➕ Added to cache: Product {} in Warehouse {} → {} units", productId, warehouseId, newStock);
        }
    }

    public List<WarehouseInfo> findWarehousesWithStock(Long productId, int requiredQuantity) {
        List<InventoryItem> inventoryItems = inventoryCache.get(productId);

        if (inventoryItems == null || inventoryItems.isEmpty()) {
            log.info("⚠️ Product {} not found in cache", productId);
            return new ArrayList<>();
        }

        List<WarehouseInfo> result = new ArrayList<>();
        inventoryItems.stream()
                .filter(item -> item.getStockOnHand() >= requiredQuantity)
                .sorted((a, b) -> Integer.compare(b.getStockOnHand(), a.getStockOnHand()))
                .forEach(item -> warehouseConfig.getList().stream()
                        .filter(w -> w.getId().equals(item.getWarehouseId()))
                        .findFirst()
                        .ifPresent(result::add));

        return result;
    }

    public List<WarehouseInfo> getAllWarehouses() {
        return warehouseConfig.getList();
    }

    public void markWarehouseOutOfStock(Long productId, Long warehouseId) {
        List<InventoryItem> items = inventoryCache.get(productId);
        if (items != null) {
            items.removeIf(item -> item.getWarehouseId().equals(warehouseId));
            log.info("🚫 Marked Warehouse {} as out of stock for Product {}", warehouseId, productId);
        }
    }

    public String getProductName(Long productId) {
        List<InventoryItem> items = inventoryCache.get(productId);
        if (items != null && !items.isEmpty()) return items.get(0).getProductName();
        return "Unknown Product";
    }

    public float getPrice(Long productId) {
        List<InventoryItem> items = inventoryCache.get(productId);
        if (items != null && !items.isEmpty()) return items.get(0).getPrice();
        return 0.0f;
    }

    public Map<Long, List<InventoryItem>> getInventorySnapshot() {
        return new ConcurrentHashMap<>(inventoryCache);
    }
}
```

## service/OrderItemService.java
```java
package com.inventory.order_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.inventory.order_service.entity.OrderItem;
import com.inventory.order_service.repository.OrderItemRepository;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderProducer orderProducer;
    private final InventoryService inventoryService;

    public OrderItemService(OrderItemRepository orderItemRepository,
                            OrderProducer orderProducer,
                            InventoryService inventoryService) {
        this.orderItemRepository = orderItemRepository;
        this.orderProducer = orderProducer;
        this.inventoryService = inventoryService;
    }

    public OrderItem createOrder(Long retailerId, Long productId, String productName, int quantity, Long warehouseId) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(generateOrderId());
        orderItem.setReferenceId(generateReferenceId());
        orderItem.setRetailerId(retailerId);
        orderItem.setProductId(productId);

        if (productName == null || productName.isEmpty()) {
            orderItem.setProductName(inventoryService.getProductName(productId));
            orderItem.setPrice(inventoryService.getPrice(productId));
        } else {
            orderItem.setProductName(productName);
            orderItem.setPrice(inventoryService.getPrice(productId));
        }

        orderItem.setQuantity(quantity);
        orderItem.setWarehouseId(warehouseId);
        orderItem.setStatus("ACCEPTED");
        orderItem.setCreatedAt(LocalDateTime.now());

        OrderItem savedOrder = orderItemRepository.save(orderItem);
        orderProducer.sendOrderMessage(savedOrder);
        return savedOrder;
    }

    public OrderItem updateOrderStatus(String orderId, String newStatus) {
        OrderItem orderItem = orderItemRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        orderItem.setStatus(newStatus);
        orderItem.setUpdatedAt(LocalDateTime.now());
        if ("COMPLETED".equals(newStatus)) orderItem.setCompletedAt(LocalDateTime.now());
        return orderItemRepository.save(orderItem);
    }

    public OrderItem getOrder(String orderId) {
        return orderItemRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public List<OrderItem> getOrdersByStatus(String status) { return orderItemRepository.findByStatus(status); }
    public List<OrderItem> getRetailerOrders(Long retailerId) { return orderItemRepository.findByRetailerId(retailerId); }
    public List<OrderItem> getAllOrders() { return orderItemRepository.findAll(); }

    public OrderItem addNotes(String orderId, String notes) {
        OrderItem orderItem = getOrder(orderId);
        orderItem.setNotes(notes);
        return orderItemRepository.save(orderItem);
    }

    private String generateOrderId() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%d-%02d-%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + datePart + "-" + uniquePart;
    }

    private String generateReferenceId() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
```

## service/OrderProducer.java
```java
package com.inventory.order_service.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.inventory.order_service.config.RabbitMQConfig;

@Service
public class OrderProducer {
    private final RabbitTemplate rabbitTemplate;

    public OrderProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderMessage(Object orderMessage) {
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_ACCEPTED_EXCHANGE,
            RabbitMQConfig.ORDER_ACCEPTED_ROUTING_KEY,
            orderMessage
        );
    }
}
```

## service/OrderRouterService.java
```java
package com.inventory.order_service.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.order_service.config.RabbitMQConfig;
import com.inventory.order_service.config.WarehouseConfig.WarehouseInfo;
import com.inventory.order_service.entity.OrderItem;
import com.inventory.order_service.repository.OrderItemRepository;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
public class OrderRouterService {

    private final InventoryService inventoryService;
    private final RabbitTemplate rabbitTemplate;
    private final OrderItemRepository orderItemRepository;

    public OrderRouterService(InventoryService inventoryService,
                               RabbitTemplate rabbitTemplate,
                               OrderItemRepository orderItemRepository) {
        this.inventoryService = inventoryService;
        this.rabbitTemplate = rabbitTemplate;
        this.orderItemRepository = orderItemRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_ACCEPTED_QUEUE)
    @Transactional
    public void routeOrder(OrderItem order) {
        log.info("🎯 Routing order: {} for product: {}", order.getOrderId(), order.getProductName());

        List<WarehouseInfo> candidates = inventoryService.findWarehousesWithStock(
            order.getProductId(), order.getQuantity()
        );

        if (candidates.isEmpty()) {
            log.warn("❌ No warehouse found for order: {}", order.getOrderId());
            order.setStatus("OUT_OF_STOCK");
            orderItemRepository.save(order);
            return;
        }

        WarehouseInfo selected = candidates.get(0);
        log.info("✅ Selected Warehouse: {}", selected.getId());

        order.setStatus("ROUTED");
        order.setWarehouseId(selected.getId());
        orderItemRepository.save(order);

        String routingKey = getRoutingKeyForWarehouse(selected.getId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_ROUTED_EXCHANGE, routingKey, order);
        log.info("📨 Order {} dispatched to warehouse {} via routing key: {}", order.getOrderId(), selected.getId(), routingKey);
    }

    private String getRoutingKeyForWarehouse(Long warehouseId) {
        if (warehouseId == 1) return RabbitMQConfig.ORDER_ROUTED_ROUTING_KEY_WAREHOUSE1;
        if (warehouseId == 2) return RabbitMQConfig.ORDER_ROUTED_ROUTING_KEY_WAREHOUSE2;
        if (warehouseId == 3) return RabbitMQConfig.ORDER_ROUTED_ROUTING_KEY_WAREHOUSE3;
        throw new IllegalArgumentException("Unknown warehouse ID: " + warehouseId);
    }
}
```

## service/OrderService.java
```java
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

    public Map<Long, List<InventoryItem>> getInventorySnapshot() {
        return inventoryService.getInventorySnapshot();
    }
}
```
