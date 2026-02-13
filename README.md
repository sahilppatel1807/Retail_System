# Retail Microservices System

A complete retail inventory management system demonstrating microservices architecture with Spring Boot and React.

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

This is a resilient, multi-instance microservices system that simulates a complex retail supply chain:

```
Warehouses (1, 2, 3) ↔ Warehouse Central ↔ Retailers (1, 2, 3) ↔ Customer ↔ Customer UI
```

- **Warehouse Instances**: Multiple warehouses managing local inventory and **full inventory movement history**
- **Warehouse Central**: Intelligent router and cache that connects retailers to available warehouses
- **Retailer Instances**: Multiple retailers with independent inventory, **purchase/sale history, and audit trails**
- **Customer Service**: Handles customer interactions and order history
- **Customer UI**: React-based frontend for end-users
- **RabbitMQ**: Message broker for real-time inventory synchronization

---

## 📦 Services

### 1. Warehouse Services (Ports 8081, 8091, 8101)

**Purpose**: Distributed inventory management

**Responsibilities**:
- Manage local stock with unique warehouse IDs (1, 2, 3)
- Sell products to retailers via Warehouse Central
- Broadcast stock changes via **RabbitMQ**
- Auto-merge duplicate products by name
- **Track every inventory movement** (ADDED, SOLD, ADJUSTED) in `warehouse_inventory_history` table
- **Record which retailer bought** each item (retailerId stored on SOLD transactions)

**Technology**: Spring Boot, PostgreSQL, RabbitMQ, JPA/Hibernate

**Directory**: `/warehouse`

---

### 2. Retailer Services (Ports 8082, 8092, 8102)

**Purpose**: Scalable retail layer between warehouses and customers

**Responsibilities**:
- Purchase from the most available warehouse via Warehouse Central
- Maintain independent retailer inventory (Ids: 1, 2, 3)
- Record sales transactions and customer orders
- Auto-merge duplicate purchases
- **Track inventory movements** (PURCHASED, SOLD) in `retailer_inventory_history` table
- **Calculate weighted average purchase price** across multiple warehouse buys
- **Link history entries to source records** via `referenceId` (purchase ID or sale ID)

**Technology**: Spring Boot, PostgreSQL, RestTemplate, JPA/Hibernate

**Directory**: `/retailer`

---

### 3. Warehouse Central (Port 8090)

**Purpose**: Intelligent routing and inventory caching layer

**Responsibilities**:
- Maintain a high-performance cache of all warehouse inventories
- Listen to RabbitMQ for real-time stock updates from warehouses
- Route retailer purchase requests to warehouses with sufficient stock
- Provide fallback mechanisms if a warehouse is unreachable

**Technology**: Spring Boot, RabbitMQ (Consumer), In-Memory Cache

**Directory**: `/warehouse-central`

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

### Service Communication Flow

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
                   │                     │                     │
                   └───────────┐         │         ┌───────────┘
                               ▼         ▼         ▼
                       ┌────────────────────────────────┐
                       │       Warehouse Central        │
                       │     (Routing & Inventory)      │◀─────┐
                       │          (Port 8090)           │      │
                       └───────────────┬────────────────┘      │
                                       │ REST                  │ AMQP
                  ┌───────────────────┼───────────────────┐   │ (Stock
                  ▼                   ▼                   ▼   │ Updates)
          ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
          │  Warehouse 1  │   │  Warehouse 2  │   │  Warehouse 3  │
          │  [Inv History]│   │  [Inv History]│   │  [Inv History]│
          │  (Port 8081)  │   │  (Port 8091)  │   │  (Port 8101)  │
          └───────┬───────┘   └───────┬───────┘   └───────┬───────┘
                  │                   │                   │
                  └──────────┐        │        ┌──────────┘
                             ▼        ▼        ▼
                     ┌─────────────────────────────────┐
                     │            RabbitMQ             │
                     │        (Message Broker)         │
                     └─────────────────────────────────┘

         (All Services) ─────▶  ┌─────────────────────┐
                                │     PostgreSQL      │
                                │  (History Tables +  │
                                │   Inventory Data)   │
                                └─────────────────────┘
```

> **📊 History Flow**: Every buy/sell/add/adjust operation at both the **Warehouse** and **Retailer** level automatically creates a history record in PostgreSQL with `stockBefore → stockAfter` snapshots, timestamps, and reference IDs linking back to the original transaction.

### Database & Messaging
- **PostgreSQL 16**: Shared database engine with isolated service schemas
- **RabbitMQ**: Event-driven architecture for inventory synchronization

### Database Tables (New in this version)

| Table | Service | Tracks |
|-------|---------|--------|
| `warehouse_inventory_history` | Warehouse | ADDED, SOLD, ADJUSTED events with retailerId |
| `retailer_inventory_history` | Retailer | PURCHASED, SOLD events with referenceId |
| `retailer_inventory` | Retailer | Current stock + weighted average purchase price |
| `sale` | Retailer | Customer sales with selling price (20% markup) |

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
docker-compose up --build -d
```

This will start:
- PostgreSQL database (5432)
- **RabbitMQ** (5672, 15672)
- **Warehouse Central** (8090)
- **3x Warehouse Instances** (8081, 8091, 8101)
- **3x Retailer Instances** (8082, 8092, 8102)
- Customer service (8083)
- React UI (3000)

### Stop All Services

```bash
# Stop containers
docker-compose down

# Stop and clear database (fresh start)
docker-compose down -v
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f warehouse-central
docker-compose logs -f warehouse1
docker-compose logs -f retailer1
docker-compose logs -f rabbitmq
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

#### Sell Item (Manual/Internal)
**POST** `/api/warehouse/buy?retailerId={rid}&itemId={id}&quantity={qty}`

#### Update Item
**PUT** `/api/warehouse/{id}`
```json
{
  "productName": "Gaming Laptop",
  "price": 1299.99,
  "stockOnHand": 100
}
```

#### 🆕 Get Warehouse Inventory History
**GET** `/api/warehouse/history`
*Returns all inventory movements (ADDED, SOLD, ADJUSTED) for the warehouse, sorted by most recent*

#### 🆕 Get Product History
**GET** `/api/warehouse/history/product/{productId}`
*Returns history for a specific product across all transaction types*

---

### Warehouse Central (http://localhost:8090)

#### Route Purchase
**POST** `/api/warehouse-central/purchase`
```json
{
  "productId": 1,
  "quantity": 5,
  "retailerId": 1
}
```
*Note: Automatically finds and calls the warehouse with the highest stock.*

#### View Inventory Cache
**GET** `/api/warehouse-central/inventory`

---

### Retailer Service (http://localhost:8082)

#### Buy from Warehouse
**POST** `/api/retailer/buy`
```json
{
  "itemId": 1,
  "quantity": 10
}
```
*Note: Auto-merges if product already exists in retailer inventory*

#### Get All Products
**GET** `/api/retailer/all`

#### Get Single Product
**GET** `/api/retailer/{id}`

#### Place Order (from customer)
**POST** `/api/retailer/orders?productId={id}&quantity={qty}&customerName={name}`

#### Update Purchase
**PUT** `/api/retailer/{id}`
```json
{
  "warehouseItemId": 1,
  "productName": "Laptop",
  "price": 999.99,
  "quantity": 20
}
```

#### 🆕 Get Current Inventory (with avg prices)
**GET** `/api/retailer/inventory`
*Shows current stock + weighted average purchase price per product*

#### 🆕 Get Specific Product Inventory
**GET** `/api/retailer/inventory/product/{productId}`

#### 🆕 Get Purchase History
**GET** `/api/retailer/purchases`
*All purchases made from warehouses*

#### 🆕 Get Sales History
**GET** `/api/retailer/sales`
*All sales made to customers*

#### 🆕 Get Specific Sale
**GET** `/api/retailer/sales/{id}`

#### 🆕 Get Full Inventory Audit Trail
**GET** `/api/retailer/inventory/history`
*Complete log of all stock changes (PURCHASED, SOLD) with stockBefore/stockAfter snapshots*

#### 🆕 Get Product-Specific History
**GET** `/api/retailer/inventory/history/product/{productId}`
*History filtered for a single product*

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

#### Get Single Order
**GET** `/api/customer/{id}`

---

## ✨ Features

### Centralized Routing
- **Warehouse Central** acts as a smart gateway
- Automatically selects warehouses based on stock availability
- Provides high availability and failure fallback

### Event-Driven Sync
- Uses **RabbitMQ** for real-time stock synchronization
- Decouples warehouses from the central routing layer
- Ensures inventory cache is always up-to-date (milliseconds latency)

### Multi-Instance Scaling
- Supports multiple Retailer and Warehouse instances out of the box
- Configuration-driven scaling via `docker-compose.yml`
- Resilient design: if one warehouse fails, others are automatically used

### Auto-Merge Logic
- Prevent duplicate product entries across the chain
- Intelligent quantity merging on purchase/creation

### 🆕 Comprehensive Inventory History Tracking
- **Warehouse History** (`warehouse_inventory_history`):
  - Tracks **ADDED** (new/restocked), **SOLD** (to retailer), **ADJUSTED** (manual edit) transactions
  - Records which **retailer** purchased items (via `retailerId` field)
  - Captures `stockBefore` → `stockAfter` snapshots for every change
- **Retailer History** (`retailer_inventory_history`):
  - Tracks **PURCHASED** (from warehouse) and **SOLD** (to customer) transactions
  - **Links to source records** via `referenceId` (maps to `purchase.id` or `sale.id`)
  - Records `priceAtTransaction` for cost tracking
- **Weighted Average Pricing**: Retailer inventory automatically calculates weighted average purchase price when buying from multiple warehouses at different prices
- **Automatic 20% Markup**: Retailer selling price is auto-calculated as `averagePurchasePrice × 1.2`
- **Timestamped & Noted**: Every history entry includes `transactionDate` and optional `notes` field (e.g., "Sold to Retailer 2", "Initial stock")

---

## 🧪 Testing the Complete Flow

### 1. Add products to warehouse
```bash
POST http://localhost:8081/api/warehouse/create
```

### 2. Retailer purchases from warehouse
```bash
POST http://localhost:8082/api/retailer/buy
```

### 3. Customer views products
```bash
GET http://localhost:8083/api/customer/products
```

### 4. Customer places order
```bash
POST http://localhost:8083/api/customer/orders
```

### 5. Verify stock and history
```bash
# Check warehouse stock
GET http://localhost:8081/api/warehouse/all

# 🆕 Check warehouse history (see ADDED and SOLD records)
GET http://localhost:8081/api/warehouse/history

# Check retailer stock (with average prices)
GET http://localhost:8082/api/retailer/inventory

# 🆕 Check retailer history (see PURCHASED and SOLD records)
GET http://localhost:8082/api/retailer/inventory/history

# Check customer orders
GET http://localhost:8083/api/customer/all
```

---

## 🌐 Web UI

Access the customer interface at: **http://localhost:3000**

1. Enter your name to login
2. Browse available products
3. Select quantity and place orders
4. Orders are saved and visible via API

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
├── warehouse/          # Warehouse microservice template
├── warehouse-central/  # Routing and caching service
├── retailer/           # Retailer microservice template
├── customer/           # Customer service
├── customer-ui/        # React frontend
├── docker-compose.yml  # Multi-service orchestration
└── README.md          # Project documentation
```

---

## 🔧 Configuration

### Database Connection
All services connect to PostgreSQL using environment variables set in `docker-compose.yml`:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

> ⚠️ **Note**: The credentials in `docker-compose.yml` are for development only. Change them for production use.

### Service Discovery
Services communicate using internal Docker network aliases:
- Retailer → Warehouse Central: `http://warehouse-central:8084`
- Warehouse Central → Warehouse 1: `http://warehouse1:8081`
- Customer → Retailer 1: `http://retailer1:8082`
- All Services → RabbitMQ: `amqp://rabbitmq:5672`

---

## 📝 Notes

- First run may take longer as Docker downloads images and builds containers
- Database persists data in Docker volume `retail_system_postgres_data`
- Services restart automatically on failure
- Auto-merge prevents duplicate product entries
- **History tables are auto-created by JPA/Hibernate** (`ddl-auto` in Spring config)

---

## 👨‍💻 Author

Sahil Patel