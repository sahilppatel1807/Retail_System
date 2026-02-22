# Retail Microservices System

A complete retail inventory management system demonstrating microservices architecture with Spring Boot and React.

> **Note**: PostgreSQL local port is **5433** mapped to Docker's 5432 to avoid conflicts with a local Postgres installation.

## 📋 Table of Contents
- [System Overview](#system-overview)
- [Services](#services)
- [Architecture](#architecture)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Features](#features)
- [Testing the Flow](#testing-the-complete-flow)

---

## 🎯 System Overview

This is a resilient, multi-instance microservices system that simulates a complex retail supply chain, powered by **asynchronous, event-driven messaging via RabbitMQ**:

```
Warehouses (1, 2, 3) ↔ [RabbitMQ] ↔ Order Service ↔ [RabbitMQ] ↔ Retailers (1, 2, 3) ↔ Customer ↔ Customer UI
```

- **Warehouse Instances**: Multiple warehouses managing local inventory with **full inventory movement history**
- **Order Service**: Intelligent async router with in-memory inventory cache
- **Retailer Instances**: Multiple retailers with independent inventory, **purchase/sale history, and audit trails**
- **Customer Service**: Handles customer interactions and order history
- **Customer UI**: React-based frontend for end-users
- **RabbitMQ**: The backbone of all asynchronous communication

---

## 📦 Services

### 1. Warehouse Services (Ports 8081, 8091, 8101)

**Purpose**: Distributed inventory management

**Responsibilities**:
- Manage local stock with unique warehouse IDs (1, 2, 3)
- On startup, **broadcast all current stock** to Order Service via RabbitMQ
- Fulfill orders received via RabbitMQ queue
- Report completion (with price) back to Order Service
- **Track every inventory movement** (ADDED, SOLD, ADJUSTED) in `warehouse_inventory_history` table
- **Record which retailer bought** each item (retailerId stored on SOLD transactions)

**Technology**: Spring Boot, PostgreSQL, RabbitMQ, JPA/Hibernate

**Directory**: `/warehouse`

---

### 2. Retailer Services (Ports 8082, 8092, 8102)

**Purpose**: Scalable retail layer between warehouses and customers

**Responsibilities**:
- Purchase stock via Order Service using asynchronous messaging
- Track order status via `OrderTracking` (`ACCEPTED` → `ROUTED` → `COMPLETED`)
- Maintain independent inventory with **weighted average purchase price**
- Sell to customers with a **15% profit markup**
- **Track inventory movements** (PURCHASED, SOLD) in `retailer_inventory_history` table
- **Link history entries to source records** via `referenceId` (purchase ID or sale ID)

**Technology**: Spring Boot, PostgreSQL, RestTemplate, JPA/Hibernate

**Directory**: `/retailer`

---

### 3. Order Service (Port 8090)

**Purpose**: Intelligent async routing and inventory caching layer

**Responsibilities**:
- Maintain a high-performance **in-memory cache** of all warehouse inventories
- Listen to stock updates from warehouses (`stock.updates.queue`)
- Accept purchase requests from retailers and publish to `order.accepted.queue`
- **Route orders** to the most stocked warehouse (`order.routed.warehouse.{1,2,3}`)
- Relay completion status back to the retailer's dedicated queue

**Technology**: Spring Boot, RabbitMQ (Consumer + Producer), In-Memory Cache

**Directory**: `/order-service`

---

### 4. Customer Service (Port 8083)

**Purpose**: Handles customer orders and interactions

**Responsibilities**:
- Display available products from retailers
- Accept and process customer orders
- Store order history and timestamps

**Technology**: Spring Boot, PostgreSQL, JPA/Hibernate

**Directory**: `/customer`

---

### 5. Customer UI (Port 3000)

**Purpose**: Premium React web interface for customers

**Technology**: React.js, CSS3 (Glassmorphism, Animations)

**Directory**: `/customer-ui`

---

## 🏗️ Architecture

### Service Communication & Queue Flow

```text
                              ┌───────────────┐
                              │  Customer UI  │
                              │  (Port 3000)  │
                              └───────┬───────┘
                                      │ REST
                                      ▼
                              ┌───────────────┐
                              │  Customer Svc │
                              │  (Port 8083)  │
                              └───────┬───────┘
                                      │ REST
                ┌─────────────────────┼─────────────────────┐
                ▼                     ▼                     ▼
        ┌───────────────┐     ┌───────────────┐     ┌───────────────┐
        │  Retailer 1   │     │  Retailer 2   │     │  Retailer 3   │
        │  [Inv History]│     │  [Inv History]│     │  [Inv History]│
        │  (Port 8082)  │     │  (Port 8092)  │     │  (Port 8102)  │
        └───────┬───────┘     └───────┬───────┘     └───────┬───────┘
                │           HTTP POST /purchase (REST)       │
                └──────────────────┬────────────────────────┘
                                   ▼
                       ┌───────────────────────┐
                       │     Order Service     │
                       │     (Port 8090)       │◀─────────────────┐
                       │  [In-Memory Cache]    │                  │ AMQP
                       └───────────────────────┘            (stock.updates.queue)
                         │ Publishes to                          │
                         │ order.accepted.queue                  │
                         ▼                                       │
              ┌──────────────────────────────────────────────────┴───┐
              │                      RabbitMQ                        │
              │                                                       │
              │  ┌─────────────────────────────────────────────┐     │
              │  │           order.accepted.queue              │     │
              │  │     (Consumed by OrderRouterService)        │     │
              │  └──────────────────┬──────────────────────────┘     │
              │                     │ Routes to best warehouse        │
              │  ┌──────────────────▼──────────────────────────┐     │
              │  │       order.routed.warehouse.1              │     │
              │  │       order.routed.warehouse.2              │     │
              │  │       order.routed.warehouse.3              │     │
              │  └──────────────────┬──────────────────────────┘     │
              │                     │ Consumed by Warehouse           │
              │  ┌──────────────────▼──────────────────────────┐     │
              │  │           status.update.queue               │     │
              │  │   Warehouse → Order Service (COMPLETED)     │     │
              │  └──────────────────┬──────────────────────────┘     │
              │                     │ Forwarded to Retailer           │
              │  ┌──────────────────▼──────────────────────────┐     │
              │  │       retailer.status.{retailerId}          │     │
              │  │   Order Service → Retailer (final notify)   │     │
              │  └─────────────────────────────────────────────┘     │
              └───────────────────────────────────────────────────────┘
                              │ Consumed by:
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
      ┌───────────────┐ ┌───────────────┐ ┌───────────────┐
      │  Warehouse 1  │ │  Warehouse 2  │ │  Warehouse 3  │
      │  [Inv History]│ │  [Inv History]│ │  [Inv History]│
      │  (Port 8081)  │ │  (Port 8091)  │ │  (Port 8101)  │
      └───────────────┘ └───────────────┘ └───────────────┘

      ┌──────────────────────────────────────────────────────────────┐
      │                        PostgreSQL                            │
      │   warehouse_schema  │  retailer_schema  │  order_svc_schema  │
      └──────────────────────────────────────────────────────────────┘
```

### ⚡ Order Lifecycle Summary

| Step | Component | Action |
|------|-----------|--------|
| 1 | **Retailer** | `POST /retailer/buy` → calls Order Service via HTTP |
| 2 | **Order Service** | Saves order (`ACCEPTED`), publishes to `order.accepted.queue` |
| 3 | **Order Service** | `OrderRouterService` picks it up, finds best warehouse, routes to `order.routed.warehouse.{N}` (→ `ROUTED`) |
| 4 | **Warehouse** | `OrderConsumer` picks it up, deducts stock, sends `COMPLETED` via `status.update.queue` |
| 5 | **Order Service** | `StatusUpdateConsumer` receives it, updates to `COMPLETED`, forwards to `retailer.status.{id}` |
| 6 | **Retailer** | `StatusUpdateConsumer` receives it, updates `OrderTracking` to `COMPLETED`, adds stock to inventory |

> **📊 History Flow**: Every buy/sell/add/adjust operation at both the **Warehouse** and **Retailer** level automatically creates a history record in PostgreSQL with `stockBefore → stockAfter` snapshots, timestamps, and reference IDs linking back to the original transaction.

### Database Tables

| Table | Service | Tracks |
|-------|---------|--------|
| `warehouse_inventory_history` | Warehouse | ADDED, SOLD, ADJUSTED events with retailerId |
| `retailer_inventory_history` | Retailer | PURCHASED, SOLD events with referenceId |
| `retailer_inventory` | Retailer | Current stock + weighted average purchase price |
| `sale` | Retailer | Customer sales with selling price (15% markup) |
| `order_item` | Order Service | All routed orders and their statuses |
| `order_tracking` | Retailer | Per-order status tracking |

---

## 🚀 Running the Application

### Prerequisites
- Docker Desktop
- Docker Compose

### Start All Services

```bash
# Navigate to project directory
cd retail_system

# Start all containers
docker compose up --build -d
```

This starts:
- PostgreSQL database (local port **5433** → docker **5432**)
- **RabbitMQ** (5672, Management UI: **15672**)
- **Order Service** (8090)
- **3× Warehouse Instances** (8081, 8091, 8101)
- **3× Retailer Instances** (8082, 8092, 8102)
- Customer Service (8083)
- React UI (3000)

### Stop All Services

```bash
# Stop containers
docker compose down

# Stop and clear database (fresh start)
docker compose down -v
```

### View Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f order-service
docker compose logs -f warehouse1
docker compose logs -f retailer1
docker compose logs -f rabbitmq
```

---

## 📡 API Endpoints

### Warehouse Service (http://localhost:8081)

#### Add/Update Item
**POST** `/api/warehouse/create`
```json
{
  "productName": "Laptop",
  "price": 999.99,
  "stockOnHand": 50
}
```
*Note: If product name exists, quantities are automatically merged*

#### Get All Items
**GET** `/api/warehouse/all`

#### Get Single Item
**GET** `/api/warehouse/{id}`

#### Update Item
**PUT** `/api/warehouse/{id}`

#### Get Warehouse Inventory History
**GET** `/api/warehouse/history`

#### Get Product History
**GET** `/api/warehouse/history/product/{productId}`

---

### Order Service (http://localhost:8090)

#### Route Purchase
**POST** `/api/order-service/purchase`
```json
{
  "productId": 1,
  "quantity": 5,
  "retailerId": 1
}
```
*Automatically routes to the warehouse with the highest stock.*

#### View Inventory Cache
**GET** `/api/order-service/inventory`

#### View All Orders
**GET** `/api/order-service/orders`

#### View Specific Order
**GET** `/api/order-service/orders/{orderId}`

---

### Retailer Service (http://localhost:8082)

#### Buy from Warehouse (Async)
**POST** `/api/retailer/buy`
```json
{
  "itemId": 1,
  "quantity": 10
}
```

#### Track Order Status
**GET** `/api/retailer/track/{orderId}`
*Returns: `id`, `orderId`, `status`, `placedAt`, `updatedAt`*

#### Track All Orders
**GET** `/api/retailer/track/all`

#### Get Current Inventory (with avg prices)
**GET** `/api/retailer/inventory`

#### Get Specific Product Inventory
**GET** `/api/retailer/inventory/product/{productId}`

#### Get Purchase History
**GET** `/api/retailer/purchases`

#### Get Sales History
**GET** `/api/retailer/sales`

#### Get Full Inventory Audit Trail
**GET** `/api/retailer/inventory/history`

#### Sell to Customer (applies 15% markup)
**POST** `/api/retailer/orders?productId={id}&quantity={qty}&customerName={name}`

---

### Customer Service (http://localhost:8083)

#### View Available Products
**GET** `/api/customer/products`

#### Place Order
**POST** `/api/customer/orders`
```json
{
  "productId": 1,
  "quantity": 2,
  "customerName": "Sahil Patel"
}
```

#### Get All Orders
**GET** `/api/customer/all`

---

## 🧪 Testing the Complete Flow

### 1. Seed warehouse stock
```bash
curl -X POST http://localhost:8081/api/warehouse/create \
     -H "Content-Type: application/json" \
     -d '{"productName": "Laptop", "price": 1000.0, "stockOnHand": 50}'
```

### 2. Verify Order Service cache is updated
```bash
curl http://localhost:8090/api/order-service/inventory
```

### 3. Retailer buys stock (async flow begins)
```bash
curl -X POST http://localhost:8082/api/retailer/buy \
     -H "Content-Type: application/json" \
     -d '{"itemId": 1, "quantity": 5}'
```
> Copy the `orderId` from the response.

### 4. Track the order status
```bash
curl http://localhost:8082/api/retailer/track/{orderId}
# Status moves: ACCEPTED → ROUTED → COMPLETED
```

### 5. Check retailer inventory (stock arrived!)
```bash
curl http://localhost:8082/api/retailer/inventory
```

### 6. Sell to a customer (15% profit)
```bash
curl -X POST "http://localhost:8082/api/retailer/orders?productId=1&quantity=1&customerName=Alice"
# Laptop bought at $1000 → Sold at $1150
```

### 🔬 Resilience Test (Forces RabbitMQ queuing)
```bash
# Stop ALL warehouses to force queuing
docker stop retail_warehouse1 retail_warehouse2 retail_warehouse3

# Place order — message waits in RabbitMQ queue
curl -X POST http://localhost:8082/api/retailer/buy \
     -H "Content-Type: application/json" \
     -d '{"itemId": 1, "quantity": 2}'

# Check RabbitMQ UI: http://localhost:15672
# Go to Queues → order.routed.warehouse.* → see 1 message "Ready"

# Restart warehouses — message is consumed instantly
docker start retail_warehouse1 retail_warehouse2 retail_warehouse3
```

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| **Async Order Routing** | Orders flow via RabbitMQ, never blocking the Retailer |
| **Auto Cache Sync** | Warehouses broadcast stock on startup and on every change |
| **Load Balancing** | Order Service picks the warehouse with most stock |
| **Resilient Messaging** | Durable queues hold messages if a warehouse is offline |
| **15% Profit Margin** | Retailer selling price = `averagePurchasePrice × 1.15` |
| **Weighted Avg Price** | Retailer calculates average cost across multiple warehouse buys |
| **Full Audit Trail** | Every stock movement recorded with before/after snapshots |
| **Order Tracking** | Track orders from `ACCEPTED` → `ROUTED` → `COMPLETED` |

---

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.x, Java 17+
- **Frontend**: React.js (Modern ES6+)
- **Messaging**: RabbitMQ 3.12+
- **Database**: PostgreSQL 16
- **Containerization**: Docker, Docker Compose

---

## 📁 Project Structure

```
retail_system/
├── warehouse/          # Warehouse microservice
├── order-service/      # Async routing & caching service
├── retailer/           # Retailer microservice
├── customer/           # Customer service
├── customer-ui/        # React frontend
├── docker-compose.yml  # Multi-service orchestration
└── README.md           # Project documentation
```

---

## 🔧 Configuration

### Service Ports
| Service | External Port |
|---------|--------------|
| Warehouse 1 | 8081 |
| Warehouse 2 | 8091 |
| Warehouse 3 | 8101 |
| Order Service | 8090 |
| Retailer 1 | 8082 |
| Retailer 2 | 8092 |
| Retailer 3 | 8102 |
| Customer | 8083 |
| Customer UI | 3000 |
| RabbitMQ (AMQP) | 5672 |
| RabbitMQ (UI) | 15672 |
| PostgreSQL | **5433** (→ 5432 inside Docker) |

### Internal Docker Hostnames
- Retailer → Order Service: `http://order-service:8084`
- Order Service → Warehouses: `http://warehouse1:8081`, `http://warehouse2:8091`, etc.
- Customer → Retailer 1: `http://retailer1:8082`
- All Services → RabbitMQ: `amqp://rabbitmq:5672`
- All Services → PostgreSQL: `jdbc:postgresql://postgres:5432/retail_system`

---

## 📝 Notes

- First run may take longer as Docker downloads images and builds containers
- Database persists data in Docker volume `retail_system_postgres_data`
- Services restart automatically on failure (`restart: on-failure`)
- **History tables are auto-created by JPA/Hibernate** (`ddl-auto: update` in Spring config)
- Auto-merge prevents duplicate product entries

---

## 👨‍💻 Author

Sahil Patel