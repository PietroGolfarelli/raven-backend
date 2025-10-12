# Raven Backend - Quarkus + DynamoDB

Backend REST API per il progetto Raven, costruito con Quarkus e integrato con AWS DynamoDB.

## 📋 Indice

- [Caratteristiche](#caratteristiche)
- [Prerequisiti](#prerequisiti)
- [Configurazione AWS](#configurazione-aws)
- [Installazione](#installazione)
- [Esecuzione](#esecuzione)
- [API Endpoints](#api-endpoints)
- [Modelli di Dati](#modelli-di-dati)
- [Struttura del Progetto](#struttura-del-progetto)
- [Testing](#testing)
- [Deployment](#deployment)

## ✨ Caratteristiche

- **Quarkus Framework**: Framework Java moderno e performante
- **AWS DynamoDB**: Database NoSQL scalabile e gestito
- **REST API**: Endpoints RESTful per gestione di Categories, Products e Orders
- **OpenAPI/Swagger**: Documentazione API automatica
- **Health Check**: Endpoint per monitoraggio dello stato dell'applicazione
- **Production Ready**: Gestione errori, logging strutturato, validazione dati

## 🔧 Prerequisiti

- Java 17 o superiore
- Maven 3.8+ 
- Account AWS con accesso a DynamoDB
- Credenziali AWS configurate

## ⚙️ Configurazione AWS

### 1. Tabelle DynamoDB

Le seguenti tabelle devono essere create in AWS DynamoDB (regione: eu-central-1):

#### raven-dev-categories
- **Partition Key**: `id` (String)

#### raven-dev-products
- **Partition Key**: `id` (String)
- **Global Secondary Index**: `products_by_category`
  - **Hash Key**: `categoryId` (String)

#### raven-dev-orders
- **Partition Key**: `id` (String)

### 2. Configurazione Credenziali AWS

Il progetto utilizza il **Default Credential Provider Chain** di AWS, che cerca le credenziali nel seguente ordine:

1. **Variabili d'ambiente**:
   ```bash
   export AWS_ACCESS_KEY_ID=your-access-key-id
   export AWS_SECRET_ACCESS_KEY=your-secret-access-key
   export AWS_REGION=eu-central-1
   ```

2. **File di credenziali AWS** (`~/.aws/credentials`):
   ```ini
   [default]
   aws_access_key_id = your-access-key-id
   aws_secret_access_key = your-secret-access-key
   ```

3. **IAM Role** (quando deployato su EC2, ECS, Lambda, etc.)

4. **Credenziali statiche** (non raccomandato per produzione):
   Decommenta e configura in `application.properties`:
   ```properties
   quarkus.dynamodb.aws.credentials.type=static
   quarkus.dynamodb.aws.credentials.static-provider.access-key-id=your-key
   quarkus.dynamodb.aws.credentials.static-provider.secret-access-key=your-secret
   ```

### 3. IAM Permissions

L'utente/role AWS deve avere i seguenti permessi DynamoDB:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:UpdateItem",
        "dynamodb:DeleteItem",
        "dynamodb:Scan",
        "dynamodb:Query"
      ],
      "Resource": [
        "arn:aws:dynamodb:eu-central-1:*:table/raven-dev-categories",
        "arn:aws:dynamodb:eu-central-1:*:table/raven-dev-products",
        "arn:aws:dynamodb:eu-central-1:*:table/raven-dev-orders",
        "arn:aws:dynamodb:eu-central-1:*:table/raven-dev-products/index/products_by_category"
      ]
    }
  ]
}
```

## 📦 Installazione

1. **Clona il repository**:
   ```bash
   git clone <repository-url>
   cd quarkus-dynamodb-raven
   ```

2. **Compila il progetto**:
   ```bash
   ./mvnw clean package
   ```

## 🚀 Esecuzione

### Modalità Development

```bash
./mvnw quarkus:dev
```

L'applicazione sarà disponibile su `http://localhost:8080`

### Modalità Production

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

## 🌐 API Endpoints

### Categories

- `GET /api/categories` - Ottieni tutte le categorie
- `GET /api/categories/{id}` - Ottieni una categoria per ID
- `POST /api/categories` - Crea una nuova categoria
- `PUT /api/categories/{id}` - Aggiorna una categoria
- `DELETE /api/categories/{id}` - Elimina una categoria

### Products

- `GET /api/products` - Ottieni tutti i prodotti
- `GET /api/products/{id}` - Ottieni un prodotto per ID
- `GET /api/products/category/{categoryId}` - Ottieni prodotti per categoria (usa GSI)
- `POST /api/products` - Crea un nuovo prodotto
- `PUT /api/products/{id}` - Aggiorna un prodotto
- `DELETE /api/products/{id}` - Elimina un prodotto

### Orders

- `GET /api/orders` - Ottieni tutti gli ordini
- `GET /api/orders/{id}` - Ottieni un ordine per ID
- `POST /api/orders` - Crea un nuovo ordine
- `PUT /api/orders/{id}` - Aggiorna un ordine
- `PATCH /api/orders/{id}/status` - Aggiorna solo lo status di un ordine
- `DELETE /api/orders/{id}` - Elimina un ordine

### Utilità

- `GET /swagger-ui` - Documentazione API interattiva (Swagger UI)
- `GET /health` - Health check endpoint

## 📊 Modelli di Dati

### Category

```json
{
  "id": "uuid",
  "name": "string",
  "color": "#hex-color",
  "sortOrder": 0,
  "description": "string",
  "icon": "string"
}
```

### Product

```json
{
  "id": "uuid",
  "categoryId": "uuid",
  "name": "string",
  "description": "string",
  "price": 0.0,
  "imageUrl": "string",
  "taxRateId": "string",
  "visibleOn": {
    "pos": true,
    "app": true
  },
  "ingredients": ["string"],
  "allergens": ["string"]
}
```

### Order

```json
{
  "id": "uuid",
  "status": "NEW|ACCEPTED|IN_PROGRESS|READY|COMPLETED|CANCELED",
  "source": "pos|mobile|restaurant_fe",
  "channel": "counter|takeaway",
  "etaMinutes": 0,
  "customer": {
    "name": "string",
    "phone": "string",
    "email": "string"
  },
  "notes": "string",
  "items": [
    {
      "productId": "uuid",
      "productName": "string",
      "quantity": 0,
      "price": 0.0,
      "notes": "string"
    }
  ],
  "createdAt": "ISO-8601",
  "updatedAt": "ISO-8601"
}
```

## 📁 Struttura del Progetto

```
src/main/java/com/raven/
├── api/                    # REST Resources (Controllers)
│   ├── CategoryResource.java
│   ├── ProductResource.java
│   └── OrderResource.java
├── model/                  # Domain Models
│   ├── Category.java
│   ├── Product.java
│   ├── Order.java
│   ├── Customer.java
│   ├── OrderItem.java
│   └── VisibleOn.java
├── repository/             # Data Access Layer
│   ├── CategoryRepository.java
│   ├── ProductRepository.java
│   └── OrderRepository.java
└── util/                   # Utility Classes
    ├── DynamoDBKeyManager.java
    ├── DynamoDBBuilder.java
    ├── DynamoDBSerializer.java
    └── DynamoDBDeserializer.java
```

## 🧪 Testing

### Test con curl

**Creare una categoria**:
```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bevande",
    "color": "#10b981",
    "sortOrder": 1,
    "description": "Bevande fresche",
    "icon": "drink"
  }'
```

**Creare un prodotto**:
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": "category-id-here",
    "name": "Coca Cola",
    "description": "Bevanda gassata",
    "price": 2.50,
    "visibleOn": {
      "pos": true,
      "app": true
    }
  }'
```

**Creare un ordine**:
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
      "phone": "+39123456789"
    },
    "items": [
      {
        "productId": "product-id-here",
        "productName": "Coca Cola",
        "quantity": 2,
        "price": 2.50
      }
    ]
  }'
```

## 🚢 Deployment

### Build Native Image (GraalVM)

Per compilare un'immagine native per prestazioni ottimali:

```bash
./mvnw package -Pnative
```

### Docker

Il progetto include Dockerfile per deployment containerizzato:

```bash
docker build -f src/main/docker/Dockerfile.jvm -t raven-backend:latest .
docker run -p 8080:8080 raven-backend:latest
```

### AWS App Runner / ECS

Il progetto è pronto per essere deployato su AWS App Runner o ECS. Assicurati che il container abbia:
- IAM role con permessi DynamoDB
- Variabili d'ambiente per configurazione (se necessario)

## 🔍 Troubleshooting

### Credenziali AWS non trovate

```
Error: Unable to load credentials from any of the providers in the chain
```

**Soluzione**: Verifica che le credenziali AWS siano configurate correttamente (vedi [Configurazione Credenziali AWS](#2-configurazione-credenziali-aws))

### Tabella DynamoDB non trovata

```
Error: Cannot do operations on a non-existent table
```

**Soluzione**: Verifica che le tabelle DynamoDB siano state create nella regione corretta (eu-central-1)

### Errori di permessi

```
Error: User is not authorized to perform: dynamodb:PutItem
```

**Soluzione**: Verifica che l'utente/role IAM abbia i permessi necessari (vedi [IAM Permissions](#3-iam-permissions))

## 📝 Note di Sviluppo

- **ID Generation**: Gli ID vengono generati automaticamente come UUID v4 se non forniti
- **Timestamps**: I timestamp degli ordini (createdAt, updatedAt) vengono gestiti automaticamente
- **Validazione**: Tutte le API eseguono validazione dei dati in input
- **Error Handling**: Gestione errori consistente con risposte HTTP appropriate
- **Logging**: Logging strutturato con diversi livelli (DEBUG in dev, INFO in prod)

## 📄 Licenza

Questo progetto è proprietario di Raven.

## 👥 Contatti

Per domande o supporto, contattare il team di sviluppo Raven.
