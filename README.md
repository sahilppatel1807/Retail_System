# Retail Microservices System

A complete retail inventory management system demonstrating microservices architecture with Spring Boot and React.

## 📋 Table of Contents
- [System Overview](#system-overview)
- [Services](#services)
- [Architecture](#architecture)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Features](#features)
---

## 🎯 System Overview

This is a three-tier microservices system that simulates a complete retail supply chain:

```
Warehouse → Retailer → Customer
```

- **Warehouse** manages inventory and supplies products to retailers
- **Retailer** purchases from warehouse and sells to customers
- **Customer** browses products and places orders
- **Customer UI** provides a React-based frontend for customers

---

## 📦 Services

### 1. Warehouse Service (Port 8081)

**Purpose**: Manages the main inventory warehouse

**Responsibilities**:
- Store product inventory with stock levels
- Sell products to retailers
- Track warehouse stock depletion
- Auto-merge duplicate products by name

**Technology**: Spring Boot, PostgreSQL, JPA/Hibernate

**Directory**: `/warehouse`

---

### 2. Retailer Service (Port 8082)

**Purpose**: Acts as the middleman between warehouse and customers

**Responsibilities**:
- Purchase products from warehouse
- Maintain retailer inventory
- Sell products to customers
- Record sales transactions
- Auto-merge duplicate purchases by name

**Technology**: Spring Boot, PostgreSQL, JPA/Hibernate, RestTemplate

**Directory**: `/retailer`

---

### 3. Customer Service (Port 8083)

**Purpose**: Handles customer orders and interactions

**Responsibilities**:
- Display available products from retailer
- Accept and process customer orders
- Store order history
- Communicate with retailer service

**Technology**: Spring Boot, PostgreSQL, JPA/Hibernate, RestTemplate

**Directory**: `/customer`

---

### 4. Customer UI (Port 3000)

**Purpose**: Web interface for customers

**Responsibilities**:
- User-friendly product browsing
- Order placement interface
- Real-time product availability

**Technology**: React.js

**Directory**: `/customer-ui`

---

## 🏗️ Architecture

### Service Communication Flow

```
┌─────────────┐
│  Warehouse  │ (Inventory Source)
│   :8081     │
└──────┬──────┘
       │ Supplies
       ▼
┌─────────────┐
│  Retailer   │ (Middleman)
│   :8082     │
└──────┬──────┘
       │ Sells
       ▼
┌─────────────┐      ┌──────────────┐
│  Customer   │◄─────│ Customer UI  │
│   :8083     │      │   :3000      │
└─────────────┘      └──────────────┘
```

### Database

All services share a single **PostgreSQL 16** database (`retail_system`) but use separate tables:
- Warehouse: `item` table
- Retailer: `retailer` and `sale` tables
- Customer: `customer_service` table

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
- PostgreSQL database (port 5432)
- Warehouse service (port 8081)
- Retailer service (port 8082)
- Customer service (port 8083)
- React UI (port 3000)

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
docker-compose logs -f warehouse
docker-compose logs -f retailer
docker-compose logs -f customer
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

#### Sell Item (Manual)
**POST** `/api/warehouse/buy?itemId={id}&quantity={qty}`

#### Update Item
**PUT** `/api/warehouse/{id}`
```json
{
  "productName": "Gaming Laptop",
  "price": 1299.99,
  "stockOnHand": 100
}
```

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

### Auto-Merge Functionality
- **Warehouse**: Adding items with the same product name automatically merges stock quantities
- **Retailer**: Buying the same product multiple times merges inventory
- **Latest price always wins** when merging

### Stock Management
- Real-time stock tracking across all services
- Automatic deduction when products are sold
- Prevents overselling with stock validation

### Order Tracking
- Complete order history for customers
- Timestamps for all transactions
- Customer name association

### Microservices Communication
- RESTful API communication between services
- RestTemplate for HTTP calls
- Independent service deployment

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

### 5. Verify stock changes
```bash
GET http://localhost:8081/api/warehouse/all
GET http://localhost:8082/api/retailer/all
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

- **Backend**: Spring Boot 3.x, Java
- **Frontend**: React.js
- **Database**: PostgreSQL 16
- **ORM**: JPA/Hibernate
- **Containerization**: Docker, Docker Compose
- **HTTP Client**: RestTemplate

---

## 📁 Project Structure

```
retail_system/
├── warehouse/          # Warehouse microservice
├── retailer/           # Retailer microservice
├── customer/           # Customer microservice
├── customer-ui/        # React frontend
├── docker-compose.yml  # Docker orchestration
└── README.md          # This file
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
Services communicate using container hostnames:
- Retailer → Warehouse: `http://warehouse:8081`
- Customer → Retailer: `http://retailer:8082`

---

## 📝 Notes

- First run may take longer as Docker downloads images and builds containers
- Database persists data in Docker volume `retail_system_postgres_data`
- Services restart automatically on failure
- Auto-merge prevents duplicate product entries

---

## 👨‍💻 Author

Sahil Patel