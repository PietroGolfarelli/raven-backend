
package com.raven.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for serializing Java objects to DynamoDB AttributeValue
 */
@ApplicationScoped
public class DynamoDBSerializer {
    
    private static final Logger LOG = Logger.getLogger(DynamoDBSerializer.class);
    
    @Inject
    ObjectMapper objectMapper;
    
    /**
     * Serialize an object to a map of AttributeValues
     * 
     * @param object The object to serialize
     * @return Map of attribute names to AttributeValues
     */
    @SuppressWarnings("unchecked")
    public Map<String, AttributeValue> serialize(Object object) {
        try {
            // Convert object to Map using Jackson
            Map<String, Object> map = objectMapper.convertValue(object, Map.class);
            return convertMapToAttributeValues(map);
        } catch (Exception e) {
            LOG.errorf(e, "Error serializing object: %s", object.getClass().getName());
            throw new RuntimeException("Failed to serialize object to DynamoDB format", e);
        }
    }
    
    /**
     * Convert a Map to DynamoDB AttributeValues
     * 
     * @param map The map to convert
     * @return Map of AttributeValues
     */
    private Map<String, AttributeValue> convertMapToAttributeValues(Map<String, Object> map) {
        Map<String, AttributeValue> result = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value != null) {
                result.put(key, toAttributeValue(value));
            }
        }
        
        return result;
    }
    
    /**
     * Convert a Java object to an AttributeValue
     * 
     * @param value The value to convert
     * @return AttributeValue
     */
    @SuppressWarnings("unchecked")
    private AttributeValue toAttributeValue(Object value) {
        if (value == null) {
            return AttributeValue.builder().nul(true).build();
        }
        
        // String
        if (value instanceof String) {
            return AttributeValue.builder().s((String) value).build();
        }
        
        // Number
        if (value instanceof Number) {
            return AttributeValue.builder().n(value.toString()).build();
        }
        
        // Boolean
        if (value instanceof Boolean) {
            return AttributeValue.builder().bool((Boolean) value).build();
        }
        
        // List
        if (value instanceof List) {
            List<AttributeValue> list = ((List<Object>) value).stream()
                .map(this::toAttributeValue)
                .collect(Collectors.toList());
            return AttributeValue.builder().l(list).build();
        }
        
        // Map (nested object)
        if (value instanceof Map) {
            Map<String, AttributeValue> map = convertMapToAttributeValues((Map<String, Object>) value);
            return AttributeValue.builder().m(map).build();
        }
        
        // For other types, try to convert to JSON string
        try {
            String json = objectMapper.writeValueAsString(value);
            return AttributeValue.builder().s(json).build();
        } catch (JsonProcessingException e) {
            LOG.warnf("Could not serialize value of type %s, using toString()", value.getClass().getName());
            return AttributeValue.builder().s(value.toString()).build();
        }
    }
    
    /**
     * Create a String AttributeValue
     */
    public AttributeValue stringValue(String value) {
        return value != null ? AttributeValue.builder().s(value).build() : null;
    }
    
    /**
     * Create a Number AttributeValue
     */
    public AttributeValue numberValue(Number value) {
        return value != null ? AttributeValue.builder().n(value.toString()).build() : null;
    }
    
    /**
     * Create a Boolean AttributeValue
     */
    public AttributeValue booleanValue(Boolean value) {
        return value != null ? AttributeValue.builder().bool(value).build() : null;
    }
    
    /**
     * Create a List AttributeValue from string list
     */
    public AttributeValue stringListValue(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<AttributeValue> list = values.stream()
            .map(s -> AttributeValue.builder().s(s).build())
            .collect(Collectors.toList());
        return AttributeValue.builder().l(list).build();
    }
}
