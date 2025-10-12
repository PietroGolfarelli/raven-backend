
package com.raven.repository;

import com.raven.model.Category;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for Category CRUD operations with DynamoDB
 */
@ApplicationScoped
public class CategoryRepository {
    
    private static final Logger LOG = Logger.getLogger(CategoryRepository.class);
    
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
    
    @ConfigProperty(name = "dynamodb.table.categories")
    String tableName;
    
    /**
     * Create a new category
     * 
     * @param category The category to create
     * @return The created category with generated ID
     */
    public Category create(Category category) {
        try {
            // Generate ID if not provided
            if (category.getId() == null || category.getId().isBlank()) {
                category.setId(keyManager.generateId());
            } else {
                keyManager.validateId(category.getId(), "Category ID");
            }
            
            // Serialize and save
            Map<String, AttributeValue> item = serializer.serialize(category);
            PutItemRequest request = builder.buildPutItemRequest(tableName, item);
            
            dynamoDbClient.putItem(request);
            
            LOG.infof("Created category with ID: %s", category.getId());
            return category;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error creating category: %s", category.getName());
            throw new RuntimeException("Failed to create category", e);
        }
    }
    
    /**
     * Get a category by ID
     * 
     * @param id The category ID
     * @return Optional containing the category if found
     */
    public Optional<Category> findById(String id) {
        try {
            keyManager.validateId(id, "Category ID");
            
            Map<String, AttributeValue> key = builder.buildKey(id);
            GetItemRequest request = builder.buildGetItemRequest(tableName, key);
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.hasItem() && !response.item().isEmpty()) {
                Category category = deserializer.deserialize(response.item(), Category.class);
                LOG.debugf("Found category with ID: %s", id);
                return Optional.of(category);
            }
            
            LOG.debugf("Category not found with ID: %s", id);
            return Optional.empty();
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error finding category by ID: %s", id);
            throw new RuntimeException("Failed to find category", e);
        }
    }
    
    /**
     * Get all categories
     * 
     * @return List of all categories
     */
    public List<Category> findAll() {
        try {
            LOG.infof("Attempting to scan table: %s", tableName);
            
            ScanRequest request = builder.buildScanRequest(tableName);
            
            LOG.debugf("Scan request built for table: %s", tableName);
            
            ScanResponse response = dynamoDbClient.scan(request);
            
            LOG.infof("Scan successful for table: %s, scanned count: %d", 
                tableName, response.count());
            
            List<Category> categories = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                categories.add(deserializer.deserialize(item, Category.class));
            }
            
            LOG.infof("Found %d categories from table: %s", categories.size(), tableName);
            return categories;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "DynamoDB error finding all categories from table: %s. Error code: %s, Message: %s", 
                tableName, 
                e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "UNKNOWN",
                e.getMessage());
            throw new RuntimeException("Failed to find categories", e);
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error finding all categories from table: %s", tableName);
            throw new RuntimeException("Failed to find categories", e);
        }
    }
    
    /**
     * Update a category
     * 
     * @param id The category ID
     * @param category The updated category data
     * @return The updated category
     */
    public Category update(String id, Category category) {
        try {
            keyManager.validateId(id, "Category ID");
            
            // Ensure ID matches
            category.setId(id);
            
            // Check if category exists
            if (findById(id).isEmpty()) {
                throw new IllegalArgumentException("Category not found with ID: " + id);
            }
            
            // Serialize and update
            Map<String, AttributeValue> item = serializer.serialize(category);
            PutItemRequest request = builder.buildPutItemRequest(tableName, item);
            
            dynamoDbClient.putItem(request);
            
            LOG.infof("Updated category with ID: %s", id);
            return category;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error updating category: %s", id);
            throw new RuntimeException("Failed to update category", e);
        }
    }
    
    /**
     * Delete a category by ID
     * 
     * @param id The category ID
     * @return true if deleted, false if not found
     */
    public boolean delete(String id) {
        try {
            keyManager.validateId(id, "Category ID");
            
            // Check if exists
            if (findById(id).isEmpty()) {
                LOG.warnf("Category not found for deletion: %s", id);
                return false;
            }
            
            Map<String, AttributeValue> key = builder.buildKey(id);
            DeleteItemRequest request = builder.buildDeleteItemRequest(tableName, key);
            
            dynamoDbClient.deleteItem(request);
            
            LOG.infof("Deleted category with ID: %s", id);
            return true;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error deleting category: %s", id);
            throw new RuntimeException("Failed to delete category", e);
        }
    }
}
