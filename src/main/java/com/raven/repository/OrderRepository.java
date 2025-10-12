
package com.raven.repository;

import com.raven.model.Order;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository for Order CRUD operations with DynamoDB
 */
@ApplicationScoped
public class OrderRepository {
    
    private static final Logger LOG = Logger.getLogger(OrderRepository.class);
    
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
    
    @ConfigProperty(name = "dynamodb.table.orders")
    String tableName;
    
    /**
     * Create a new order
     * 
     * @param order The order to create
     * @return The created order with generated ID and timestamps
     */
    public Order create(Order order) {
        try {
            // Generate ID if not provided
            if (order.getId() == null || order.getId().isBlank()) {
                order.setId(keyManager.generateId());
            } else {
                keyManager.validateId(order.getId(), "Order ID");
            }
            
            // Set timestamps
            String now = Instant.now().toString();
            if (order.getCreatedAt() == null || order.getCreatedAt().isBlank()) {
                order.setCreatedAt(now);
            }
            order.setUpdatedAt(now);
            
            // Validate required fields
            validateOrder(order);
            
            // Serialize and save
            Map<String, AttributeValue> item = serializer.serialize(order);
            PutItemRequest request = builder.buildPutItemRequest(tableName, item);
            
            dynamoDbClient.putItem(request);
            
            LOG.infof("Created order with ID: %s", order.getId());
            return order;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error creating order: %s", order.getId());
            throw new RuntimeException("Failed to create order", e);
        }
    }
    
    /**
     * Get an order by ID
     * 
     * @param id The order ID
     * @return Optional containing the order if found
     */
    public Optional<Order> findById(String id) {
        try {
            keyManager.validateId(id, "Order ID");
            
            Map<String, AttributeValue> key = builder.buildKey(id);
            GetItemRequest request = builder.buildGetItemRequest(tableName, key);
            
            GetItemResponse response = dynamoDbClient.getItem(request);
            
            if (response.hasItem() && !response.item().isEmpty()) {
                Order order = deserializer.deserialize(response.item(), Order.class);
                LOG.debugf("Found order with ID: %s", id);
                return Optional.of(order);
            }
            
            LOG.debugf("Order not found with ID: %s", id);
            return Optional.empty();
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error finding order by ID: %s", id);
            throw new RuntimeException("Failed to find order", e);
        }
    }
    
    /**
     * Get all orders
     * 
     * @return List of all orders
     */
    public List<Order> findAll() {
        try {
            ScanRequest request = builder.buildScanRequest(tableName);
            ScanResponse response = dynamoDbClient.scan(request);
            
            List<Order> orders = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                orders.add(deserializer.deserialize(item, Order.class));
            }
            
            LOG.infof("Found %d orders", orders.size());
            return orders;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error finding all orders");
            throw new RuntimeException("Failed to find orders", e);
        }
    }
    
    /**
     * Update an order
     * 
     * @param id The order ID
     * @param order The updated order data
     * @return The updated order
     */
    public Order update(String id, Order order) {
        try {
            keyManager.validateId(id, "Order ID");
            
            // Ensure ID matches
            order.setId(id);
            
            // Check if order exists
            Order existingOrder = findById(id).orElseThrow(
                () -> new IllegalArgumentException("Order not found with ID: " + id)
            );
            
            // Preserve createdAt, update updatedAt
            order.setCreatedAt(existingOrder.getCreatedAt());
            order.setUpdatedAt(Instant.now().toString());
            
            // Validate required fields
            validateOrder(order);
            
            // Serialize and update
            Map<String, AttributeValue> item = serializer.serialize(order);
            PutItemRequest request = builder.buildPutItemRequest(tableName, item);
            
            dynamoDbClient.putItem(request);
            
            LOG.infof("Updated order with ID: %s", id);
            return order;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error updating order: %s", id);
            throw new RuntimeException("Failed to update order", e);
        }
    }
    
    /**
     * Update order status
     * 
     * @param id The order ID
     * @param newStatus The new status
     * @return The updated order
     */
    public Order updateStatus(String id, String newStatus) {
        try {
            keyManager.validateId(id, "Order ID");
            
            Order order = findById(id).orElseThrow(
                () -> new IllegalArgumentException("Order not found with ID: " + id)
            );
            
            order.setStatus(newStatus);
            order.setUpdatedAt(Instant.now().toString());
            
            return update(id, order);
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error updating order status: %s", id);
            throw new RuntimeException("Failed to update order status", e);
        }
    }
    
    /**
     * Delete an order by ID
     * 
     * @param id The order ID
     * @return true if deleted, false if not found
     */
    public boolean delete(String id) {
        try {
            keyManager.validateId(id, "Order ID");
            
            // Check if exists
            if (findById(id).isEmpty()) {
                LOG.warnf("Order not found for deletion: %s", id);
                return false;
            }
            
            Map<String, AttributeValue> key = builder.buildKey(id);
            DeleteItemRequest request = builder.buildDeleteItemRequest(tableName, key);
            
            dynamoDbClient.deleteItem(request);
            
            LOG.infof("Deleted order with ID: %s", id);
            return true;
            
        } catch (DynamoDbException e) {
            LOG.errorf(e, "Error deleting order: %s", id);
            throw new RuntimeException("Failed to delete order", e);
        }
    }
    
    /**
     * Validate order required fields
     */
    private void validateOrder(Order order) {
        if (order.getStatus() == null || order.getStatus().isBlank()) {
            throw new IllegalArgumentException("Order must have a status");
        }
        
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
    }
}
