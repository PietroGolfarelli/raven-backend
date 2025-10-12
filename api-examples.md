
# API Examples

Collection of example API requests for testing the Raven Backend.

## Categories

### Create a Category

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bevande",
    "color": "#10b981",
    "sortOrder": 1,
    "description": "Bevande fresche e dissetanti",
    "icon": "drink"
  }'
```

### Get All Categories

```bash
curl http://localhost:8080/api/categories
```

### Get Category by ID

```bash
curl http://localhost:8080/api/categories/{category-id}
```

### Update Category

```bash
curl -X PUT http://localhost:8080/api/categories/{category-id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bevande Premium",
    "color": "#10b981",
    "sortOrder": 1,
    "description": "Le migliori bevande",
    "icon": "drink"
  }'
```

### Delete Category

```bash
curl -X DELETE http://localhost:8080/api/categories/{category-id}
```

## Products

### Create a Product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "your-category-id",
    "name": "Coca Cola",
    "description": "Bevanda gassata al gusto cola",
    "price": 2.50,
    "imageUrl": "https://upload.wikimedia.org/wikipedia/commons/2/27/Coca_Cola_Flasche_-_Original_Taste.jpg",
    "taxRateId": "tax-rate-1",
    "visibleOn": {
      "pos": true,
      "app": true
    },
    "ingredients": ["Acqua", "Zucchero", "Anidride carbonica"],
    "allergens": []
  }'
```

### Get All Products

```bash
curl http://localhost:8080/api/products
```

### Get Product by ID

```bash
curl http://localhost:8080/api/products/{product-id}
```

### Get Products by Category

```bash
curl http://localhost:8080/api/products/category/{category-id}
```

### Update Product

```bash
curl -X PUT http://localhost:8080/api/products/{product-id} \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "your-category-id",
    "name": "Coca Cola Zero",
    "description": "Bevanda gassata al gusto cola senza zucchero",
    "price": 2.50,
    "imageUrl": "https://www.coca-cola.com/content/dam/onexp/us/en/brands/coca-cola-zero/en_coca-cola_prod_zero%20sugar%20zero%20caffeine_750x750_v1.jpg",
    "visibleOn": {
      "pos": true,
      "app": true
    },
    "ingredients": ["Acqua", "Dolcificanti", "Anidride carbonica"],
    "allergens": []
  }'
```

### Delete Product

```bash
curl -X DELETE http://localhost:8080/api/products/{product-id}
```

## Orders

### Create an Order

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "status": "NEW",
    "source": "pos",
    "channel": "counter",
    "etaMinutes": 15,
    "customer": {
      "name": "Mario Rossi",
      "phone": "+39123456789",
      "email": "mario.rossi@example.com"
    },
    "notes": "Senza ghiaccio",
    "items": [
      {
        "productId": "product-id-1",
        "productName": "Coca Cola",
        "quantity": 2,
        "price": 2.50,
        "notes": ""
      },
      {
        "productId": "product-id-2",
        "productName": "Pizza Margherita",
        "quantity": 1,
        "price": 8.00,
        "notes": "Extra mozzarella"
      }
    ]
  }'
```

### Get All Orders

```bash
curl http://localhost:8080/api/orders
```

### Get Order by ID

```bash
curl http://localhost:8080/api/orders/{order-id}
```

### Update Order

```bash
curl -X PUT http://localhost:8080/api/orders/{order-id} \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS",
    "source": "pos",
    "channel": "counter",
    "etaMinutes": 10,
    "customer": {
      "name": "Mario Rossi",
      "phone": "+39123456789",
      "email": "mario.rossi@example.com"
    },
    "notes": "Senza ghiaccio",
    "items": [
      {
        "productId": "product-id-1",
        "productName": "Coca Cola",
        "quantity": 2,
        "price": 2.50
      }
    ]
  }'
```

### Update Order Status

```bash
curl -X PATCH http://localhost:8080/api/orders/{order-id}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "READY"
  }'
```

### Delete Order

```bash
curl -X DELETE http://localhost:8080/api/orders/{order-id}
```

## Health Check

### Check Application Health

```bash
curl http://localhost:8080/health
```

## OpenAPI/Swagger

### Access Swagger UI

Open in browser:
```
http://localhost:8080/swagger-ui
```

### Get OpenAPI JSON

```bash
curl http://localhost:8080/q/openapi
```

## Complete Example Flow

```bash
#!/bin/bash

# 1. Create a category
CATEGORY_RESPONSE=$(curl -s -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bevande",
    "color": "#10b981",
    "sortOrder": 1,
    "description": "Bevande fresche",
    "icon": "drink"
  }')

CATEGORY_ID=$(echo $CATEGORY_RESPONSE | jq -r '.id')
echo "Created category: $CATEGORY_ID"

# 2. Create a product in that category
PRODUCT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d "{
    \"categoryId\": \"$CATEGORY_ID\",
    \"name\": \"Coca Cola\",
    \"description\": \"Bevanda gassata\",
    \"price\": 2.50,
    \"visibleOn\": {
      \"pos\": true,
      \"app\": true
    },
    \"ingredients\": [\"Acqua\", \"Zucchero\"],
    \"allergens\": []
  }")

PRODUCT_ID=$(echo $PRODUCT_RESPONSE | jq -r '.id')
echo "Created product: $PRODUCT_ID"

# 3. Create an order with that product
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"status\": \"NEW\",
    \"source\": \"pos\",
    \"channel\": \"counter\",
    \"etaMinutes\": 15,
    \"customer\": {
      \"name\": \"Test Customer\",
      \"phone\": \"+39123456789\"
    },
    \"items\": [
      {
        \"productId\": \"$PRODUCT_ID\",
        \"productName\": \"Coca Cola\",
        \"quantity\": 2,
        \"price\": 2.50
      }
    ]
  }")

ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.id')
echo "Created order: $ORDER_ID"

# 4. Update order status
curl -s -X PATCH http://localhost:8080/api/orders/$ORDER_ID/status \
  -H "Content-Type: application/json" \
  -d '{"status": "READY"}'

echo "Updated order status to READY"

# 5. Get all products in category
echo "Products in category $CATEGORY_ID:"
curl -s http://localhost:8080/api/products/category/$CATEGORY_ID | jq '.'
```

## Testing with HTTPie (Alternative to curl)

If you prefer HTTPie over curl:

```bash
# Install HTTPie
pip install httpie

# Create category
http POST localhost:8080/api/categories \
  name="Bevande" \
  color="#10b981" \
  sortOrder:=1 \
  description="Bevande fresche" \
  icon="drink"

# Get all categories
http GET localhost:8080/api/categories

# Create product
http POST localhost:8080/api/products \
  categoryId="your-category-id" \
  name="Coca Cola" \
  description="Bevanda gassata" \
  price:=2.50 \
  visibleOn:='{"pos":true,"app":true}' \
  ingredients:='["Acqua","Zucchero"]' \
  allergens:='[]'
```
