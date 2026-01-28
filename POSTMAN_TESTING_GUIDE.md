# Postman Testing Guide for Retail Microservices

This guide will walk you through testing all the microservices using Postman.

---

## Prerequisites

1. Ensure all Docker containers are running:
   ```bash
   docker-compose ps
   ```

2. All services should show as "Up"

---

## Postman Collection Structure

Create a new Collection in Postman called **"Retail Microservices"** with the following folders:
- 📁 Warehouse Service
- 📁 Retailer Service  
- 📁 Customer Service

---

## 1️⃣ Warehouse Service (Ports: 8081, 8091, 8101)

### 1.1 Add Items to Warehouse

**Create 3 separate requests for each warehouse:**

#### Warehouse 1: Add Laptop
- **Method**: `POST`
- **URL**: `http://localhost:8081/api/warehouse/create`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "productName": "Laptop",
  "price": 999.99,
  "stockOnHand": 50
}
```

#### Warehouse 2: Add Mouse
- **Method**: `POST`
- **URL**: `http://localhost:8091/api/warehouse/create`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "productName": "Mouse",
  "price": 29.99,
  "stockOnHand": 100
}
```

#### Warehouse 3: Add Keyboard
- **Method**: `POST`
- **URL**: `http://localhost:8101/api/warehouse/create`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "productName": "Keyboard",
  "price": 79.99,
  "stockOnHand": 75
}
```

**Expected Response** (example):
```json
{
  "id": 1,
  "productName": "Laptop",
  "price": 999.99,
  "stockOnHand": 50,
  "warehouseId": 1
}
```

---

### 1.2 Get All Items from Warehouse

#### Get All Items (Warehouse 1)
- **Method**: `GET`
- **URL**: `http://localhost:8081/api/warehouse/all`
- **Headers**: None required

**Expected Response**:
```json
[
  {
    "id": 1,
    "productName": "Laptop",
    "price": 999.99,
    "stockOnHand": 50,
    "warehouseId": 1
  },
  {
    "id": 2,
    "productName": "Mouse",
    "price": 29.99,
    "stockOnHand": 100,
    "warehouseId": 2
  },
  {
    "id": 3,
    "productName": "Keyboard",
    "price": 79.99,
    "stockOnHand": 75,
    "warehouseId": 3
  }
]
```

> **Note**: Since all warehouses share the same database, they all show all items. The `warehouseId` field indicates which warehouse owns each item.

---

### 1.3 Get Single Item

- **Method**: `GET`
- **URL**: `http://localhost:8081/api/warehouse/1`
- **Headers**: None required

---

### 1.4 Update Item

- **Method**: `PUT`
- **URL**: `http://localhost:8081/api/warehouse/1`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "productName": "Gaming Laptop",
  "price": 1299.99,
  "stockOnHand": 100
}
```

---

## 2️⃣ Retailer Service (Ports: 8082, 8092, 8102)

### 2.1 Buy from Warehouse

> **IMPORTANT**: The `warehouseId` field is **required** to specify which warehouse to buy from.

#### Retailer 1: Buy Laptops from Warehouse 1
- **Method**: `POST`
- **URL**: `http://localhost:8082/api/retailer/buy`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "warehouseId": 1,
  "itemId": 1,
  "quantity": 10
}
```

**Expected Response**:
```json
{
  "id": 1,
  "retailerId": 1,
  "warehouseId": 1,
  "warehouseItemId": 1,
  "productName": "Laptop",
  "price": 999.99,
  "quantity": 10
}
```

#### Retailer 2: Buy Mice from Warehouse 2
- **Method**: `POST`
- **URL**: `http://localhost:8092/api/retailer/buy`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "warehouseId": 2,
  "itemId": 2,
  "quantity": 20
}
```

#### Retailer 3: Buy Keyboards from Warehouse 3
- **Method**: `POST`
- **URL**: `http://localhost:8102/api/retailer/buy`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "warehouseId": 3,
  "itemId": 3,
  "quantity": 15
}
```

---

### 2.2 Get All Retailer Inventory

#### Get Retailer 1 Inventory
- **Method**: `GET`
- **URL**: `http://localhost:8082/api/retailer/all`
- **Headers**: None required

**Expected Response**:
```json
[
  {
    "id": 1,
    "retailerId": 1,
    "warehouseId": 1,
    "warehouseItemId": 1,
    "productName": "Laptop",
    "price": 999.99,
    "quantity": 10
  }
]
```

---

### 2.3 Get Single Product

- **Method**: `GET`
- **URL**: `http://localhost:8082/api/retailer/1`
- **Headers**: None required

---

### 2.4 Update Purchase

- **Method**: `PUT`
- **URL**: `http://localhost:8082/api/retailer/1`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "warehouseItemId": 1,
  "productName": "Laptop",
  "price": 999.99,
  "quantity": 20
}
```

---

## 3️⃣ Customer Service (Port: 8083)

### 3.1 View Available Products

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/customer/products`
- **Headers**: None required

**Expected Response**:
```json
[
  {
    "id": 1,
    "retailerId": 1,
    "warehouseId": 1,
    "warehouseItemId": 1,
    "productName": "Laptop",
    "price": 999.99,
    "quantity": 10
  },
  {
    "id": 2,
    "retailerId": 2,
    "warehouseId": 2,
    "warehouseItemId": 2,
    "productName": "Mouse",
    "price": 29.99,
    "quantity": 20
  }
]
```

---

### 3.2 Place Order

- **Method**: `POST`
- **URL**: `http://localhost:8083/api/customer/orders`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "productId": 1,
  "quantity": 2,
  "customerName": "Sahil Patel"
}
```

**Expected Response**:
```json
{
  "id": 1,
  "customerName": "Sahil Patel",
  "productId": 1,
  "productName": "Laptop",
  "quantity": 2,
  "price": 999.99,
  "orderTime": "2026-01-25T02:40:40.759008"
}
```

---

### 3.3 Get All Orders

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/customer/all`
- **Headers**: None required

**Expected Response**:
```json
[
  {
    "id": 1,
    "customerName": "Sahil Patel",
    "productId": 1,
    "productName": "Laptop",
    "quantity": 2,
    "price": 999.99,
    "orderTime": "2026-01-25T02:40:40.759008"
  }
]
```

---

### 3.4 Get Single Order

- **Method**: `GET`
- **URL**: `http://localhost:8083/api/customer/1`
- **Headers**: None required

---

## 🔄 Complete Testing Flow

Follow this sequence to test the entire supply chain:

### Step 1: Add Products to Warehouses
1. POST to `http://localhost:8081/api/warehouse/create` (Laptop)
2. POST to `http://localhost:8091/api/warehouse/create` (Mouse)
3. POST to `http://localhost:8101/api/warehouse/create` (Keyboard)

### Step 2: Verify Warehouse Inventory
4. GET `http://localhost:8081/api/warehouse/all`

### Step 3: Retailers Purchase from Warehouses
5. POST to `http://localhost:8082/api/retailer/buy` (Retailer 1 buys Laptops)
6. POST to `http://localhost:8092/api/retailer/buy` (Retailer 2 buys Mice)

### Step 4: Verify Stock Deduction
7. GET `http://localhost:8081/api/warehouse/all` (Check Laptop stock decreased)

### Step 5: Verify Retailer Inventory
8. GET `http://localhost:8082/api/retailer/all` (Retailer 1 should have Laptops)
9. GET `http://localhost:8092/api/retailer/all` (Retailer 2 should have Mice)

### Step 6: Customer Views Products
10. GET `http://localhost:8083/api/customer/products`

### Step 7: Customer Places Order
11. POST to `http://localhost:8083/api/customer/orders`

### Step 8: Verify Final State
12. GET `http://localhost:8082/api/retailer/all` (Retailer stock should decrease)
13. GET `http://localhost:8083/api/customer/all` (Order should be recorded)

---

## 💡 Pro Tips

### Environment Variables
Create a Postman Environment with these variables:
- `warehouse1_port`: `8081`
- `warehouse2_port`: `8091`
- `warehouse3_port`: `8101`
- `retailer1_port`: `8082`
- `retailer2_port`: `8092`
- `retailer3_port`: `8102`
- `customer_port`: `8083`
- `base_url`: `http://localhost`

Then use: `{{base_url}}:{{warehouse1_port}}/api/warehouse/create`

### Tests Tab
Add automatic tests in Postman's "Tests" tab:

```javascript
// Check status code
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// Check response time
pm.test("Response time is less than 500ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

// Validate JSON response
pm.test("Response has required fields", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('id');
    pm.expect(jsonData).to.have.property('productName');
});
```

### Save Responses
Use Postman's "Save Response" feature to capture examples for documentation.

---

## 🐛 Common Issues

### 1. Connection Refused
**Problem**: `Could not get any response`  
**Solution**: Ensure Docker containers are running: `docker-compose ps`

### 2. 500 Internal Server Error on Retailer Buy
**Problem**: `NullPointerException: warehouseId is null`  
**Solution**: Make sure to include `warehouseId` in the request body

### 3. Empty Response Array
**Problem**: GET requests return `[]`  
**Solution**: Make sure you've added items first using POST requests

---

## 📥 Import Collection (Optional)

You can create a Postman Collection JSON file with all these requests pre-configured. Let me know if you'd like me to generate one for you!

---

## 📊 Testing Checklist

- [ ] All 3 warehouses can create items
- [ ] All 3 warehouses show all items in database
- [ ] All 3 retailers can buy from specific warehouses
- [ ] Warehouse stock decreases after retailer purchase
- [ ] Retailer inventory increases after purchase
- [ ] Customer can view all available products
- [ ] Customer can place orders
- [ ] Retailer stock decreases after customer order
- [ ] Order is recorded with timestamp

---

Happy Testing! 🚀
