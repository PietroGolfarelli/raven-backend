
package com.raven.repository;

import com.raven.model.Product;
import com.raven.util.DynamoDBBuilder;
import com.raven.util.DynamoDBDeserializer;
import com.raven.util.DynamoDBKeyManager;
import com.raven.util.DynamoDBSerializer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * Repository for Product CRUD operations with DynamoDB
 */
@ApplicationScoped
public class ProductRepository {
    
    private static final Logger LOG = Logger.getLogger(ProductRepository.class);
    
    @Inject
    DynamoDbClient dynamoDbClient;
    
    @Inject
    DynamoDBSerializer serializer;
    
    @Inject
    DynamoDBDeserializer deserializer;
    
    @Inject
    DynamoDBBuilder builder;
    
    @Inject
    DynamoDBKeyManager keyManager;
    
    @ConfigProperty(name = "dynamodb.table.products")
    String tableName;
    
    @ConfigProperty(name = "dynamodb.gsi.products-by-category")
    String gsiName;
    
    /**
     * Create a new product
     * 
     * @param product The product to create
     * @return The created product with generated ID
     */
    public Product create(Product product) {
        try {
            // Generate ID if not provided
            if (product.getId() == null || product.getId().isBlank()) {
                product.setId(keyManager.generateId());
            } else {
                keyManager.validateId(product.getId(), "Product ID");
            }
            
            // Validate categoryId
            if (product.getCategoryId() == null || product.getCategoryId().isBlank()) {
                throw new IllegalArgumentException("Product must have a categoryId");
            }
            
            // Serialize and save
            Map<String, AttributeValue> item = serializer.serialize(product);
            PutItemRequest request = builder.buildPutItemRequest(tableName, item);
            
            dynamoDbClient.putItem(request);
            
            LOG.infof("Created product with ID: %s", product.getId());
            return product;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error creating product: %s", product.getName());
            throw new RuntimeException("Failed to create product", e);
        }
    }
    
    /**
     * Get a product by ID
     * 
     * @param id The product ID
     * @return Optional containing the product if found
     */
    public Optional<Product> findById(String id) {
        try {
            keyManager.validateId(id, "Product ID");
            
            Map<String, AttributeValue> key = builder.buildKey(id);
            GetItemRequest request = builder.buildGetItemRequest(tableName, key);
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.hasItem() && !response.item().isEmpty()) {
                Product product = deserializer.deserialize(response.item(), Product.class);
                LOG.debugf("Found product with ID: %s", id);
                return Optional.of(product);
            }
            
            LOG.debugf("Product not found with ID: %s", id);
            return Optional.empty();
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error finding product by ID: %s", id);
            throw new RuntimeException("Failed to find product", e);
        }
    }
    
    /**
     * Get all products
     * 
     * @return List of all products
     */
    public List<Product> findAll() {
        try {
            ScanRequest request = builder.buildScanRequest(tableName);
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<Product> products = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                products.add(deserializer.deserialize(item, Product.class));
            }
            
            LOG.infof("Found %d products", products.size());
            return products;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error finding all products");
            throw new RuntimeException("Failed to find products", e);
        }
    }
    
    /**
     * Find products by category ID using GSI
     * 
     * @param categoryId The category ID
     * @return List of products in the category
     */
    public List<Product> findByCategoryId(String categoryId) {
        try {
            keyManager.validateId(categoryId, "Category ID");
            
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#categoryId", "categoryId");
            
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":categoryId", AttributeValue.builder().s(categoryId).build());
            
            QueryRequest request = builder.buildQueryRequest(
                tableName,
                gsiName,
                "#categoryId = :categoryId",
                expressionAttributeNames,
                expressionAttributeValues
            );
            
            QueryResponse response = dynamoDbClient.query(request);
            
            List<Product> products = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                products.add(deserializer.deserialize(item, Product.class));
            }
            
            LOG.infof("Found %d products for category: %s", products.size(), categoryId);
            return products;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error finding products by category ID: %s", categoryId);
            throw new RuntimeException("Failed to find products by category", e);
        }
    }
    
    /**
     * Update a product
     * 
     * @param id The product ID
     * @param product The updated product data
     * @return The updated product
     */
    public Product update(String id, Product product) {
        try {
            keyManager.validateId(id, "Product ID");
            
            // Ensure ID matches
            product.setId(id);
            
            // Check if product exists
            if (findById(id).isEmpty()) {
                throw new IllegalArgumentException("Product not found with ID: " + id);
            }
            
            // Validate categoryId
            if (product.getCategoryId() == null || product.getCategoryId().isBlank()) {
                throw new IllegalArgumentException("Product must have a categoryId");
            }
            
            // Serialize and update
            Map<String, AttributeValue> item = serializer.serialize(product);
            PutItemRequest request = builder.buildPutItemRequest(tableName, item);
            
            dynamoDbClient.putItem(request);
            
            LOG.infof("Updated product with ID: %s", id);
            return product;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error updating product: %s", id);
            throw new RuntimeException("Failed to update product", e);
        }
    }
    
    /**
     * Delete a product by ID
     * 
     * @param id The product ID
     * @return true if deleted, false if not found
     */
    public boolean delete(String id) {
        try {
            keyManager.validateId(id, "Product ID");
            
            // Check if exists
            if (findById(id).isEmpty()) {
                LOG.warnf("Product not found for deletion: %s", id);
                return false;
            }
            
            Map<String, AttributeValue> key = builder.buildKey(id);
            DeleteItemRequest request = builder.buildDeleteItemRequest(tableName, key);
            
            dynamoDbClient.deleteItem(request);
            
            LOG.infof("Deleted product with ID: %s", id);
            return true;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error deleting product: %s", id);
            throw new RuntimeException("Failed to delete product", e);
        }
    }
}
