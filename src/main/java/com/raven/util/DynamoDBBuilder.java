
package com.raven.util;

import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for building DynamoDB request objects
 */
@ApplicationScoped
public class DynamoDBBuilder {
    
    /**
     * Build a PutItemRequest
     * 
     * @param tableName The table name
     * @param item The item attributes
     * @return PutItemRequest
     */
    public PutItemRequest buildPutItemRequest(String tableName, Map<String, AttributeValue> item) {
        return PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .build();
    }
    
    /**
     * Build a PutItemRequest with condition expression
     * 
     * @param tableName The table name
     * @param item The item attributes
     * @param conditionExpression The condition expression
     * @param expressionAttributeNames The expression attribute names
     * @return PutItemRequest
     */
    public PutItemRequest buildPutItemRequest(
            String tableName, 
            Map<String, AttributeValue> item,
            String conditionExpression,
            Map<String, String> expressionAttributeNames) {
        return PutItemRequest.builder()
            .tableName(tableName)
            .item(item)
            .conditionExpression(conditionExpression)
            .expressionAttributeNames(expressionAttributeNames)
            .build();
    }
    
    /**
     * Build a GetItemRequest
     * 
     * @param tableName The table name
     * @param key The key attributes
     * @return GetItemRequest
     */
    public GetItemRequest buildGetItemRequest(String tableName, Map<String, AttributeValue> key) {
        return GetItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();
    }
    
    /**
     * Build a DeleteItemRequest
     * 
     * @param tableName The table name
     * @param key The key attributes
     * @return DeleteItemRequest
     */
    public DeleteItemRequest buildDeleteItemRequest(String tableName, Map<String, AttributeValue> key) {
        return DeleteItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .build();
    }
    
    /**
     * Build an UpdateItemRequest
     * 
     * @param tableName The table name
     * @param key The key attributes
     * @param updateExpression The update expression
     * @param expressionAttributeNames The expression attribute names
     * @param expressionAttributeValues The expression attribute values
     * @return UpdateItemRequest
     */
    public UpdateItemRequest buildUpdateItemRequest(
            String tableName,
            Map<String, AttributeValue> key,
            String updateExpression,
            Map<String, String> expressionAttributeNames,
            Map<String, AttributeValue> expressionAttributeValues) {
        return UpdateItemRequest.builder()
            .tableName(tableName)
            .key(key)
            .updateExpression(updateExpression)
            .expressionAttributeNames(expressionAttributeNames)
            .expressionAttributeValues(expressionAttributeValues)
            .returnValues(ReturnValue.ALL_NEW)
            .build();
    }
    
    /**
     * Build a ScanRequest
     * 
     * @param tableName The table name
     * @return ScanRequest
     */
    public ScanRequest buildScanRequest(String tableName) {
        return ScanRequest.builder()
            .tableName(tableName)
            .build();
    }
    
    /**
     * Build a QueryRequest
     * 
     * @param tableName The table name
     * @param indexName The index name (optional)
     * @param keyConditionExpression The key condition expression
     * @param expressionAttributeNames The expression attribute names
     * @param expressionAttributeValues The expression attribute values
     * @return QueryRequest
     */
    public QueryRequest buildQueryRequest(
            String tableName,
            String indexName,
            String keyConditionExpression,
            Map<String, String> expressionAttributeNames,
            Map<String, AttributeValue> expressionAttributeValues) {
        
        QueryRequest.Builder builder = QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression(keyConditionExpression)
            .expressionAttributeNames(expressionAttributeNames)
            .expressionAttributeValues(expressionAttributeValues);
        
        if (indexName != null && !indexName.isBlank()) {
            builder.indexName(indexName);
        }
        
        return builder.build();
    }
    
    /**
     * Build a key map for simple partition key
     * 
     * @param id The partition key value
     * @return Key map
     */
    public Map<String, AttributeValue> buildKey(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(id).build());
        return key;
    }
}
