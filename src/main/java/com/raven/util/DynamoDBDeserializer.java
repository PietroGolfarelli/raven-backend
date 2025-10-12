
package com.raven.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for deserializing DynamoDB AttributeValue to Java objects
 */
@ApplicationScoped
public class DynamoDBDeserializer {
    
    private static final Logger LOG = Logger.getLogger(DynamoDBDeserializer.class);
    
    @Inject
    ObjectMapper objectMapper;
    
    /**
     * Deserialize a map of AttributeValues to a Java object
     * 
     * @param attributes The map of AttributeValues
     * @param targetClass The target class type
     * @return The deserialized object
     */
    public <T> T deserialize(Map<String, AttributeValue> attributes, Class<T> targetClass) {
        try {
            Map<String, Object> map = convertAttributeValuesToMap(attributes);
            return objectMapper.convertValue(map, targetClass);
        } catch (Exception e) {
            LOG.errorf(e, "Error deserializing to class: %s", targetClass.getName());
            throw new RuntimeException("Failed to deserialize DynamoDB item to object", e);
        }
    }
    
    /**
     * Convert DynamoDB AttributeValues to a Map
     * 
     * @param attributes The attributes to convert
     * @return Map of Java objects
     */
    private Map<String, Object> convertAttributeValuesToMap(Map<String, AttributeValue> attributes) {
        Map<String, Object> result = new HashMap<>();
        
        for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
            String key = entry.getKey();
            AttributeValue value = entry.getValue();
            result.put(key, toJavaObject(value));
        }
        
        return result;
    }
    
    /**
     * Convert an AttributeValue to a Java object
     * 
     * @param attributeValue The AttributeValue to convert
     * @return Java object
     */
    private Object toJavaObject(AttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        
        // Null
        if (Boolean.TRUE.equals(attributeValue.nul())) {
            return null;
        }
        
        // String
        if (attributeValue.s() != null) {
            return attributeValue.s();
        }
        
        // Number
        if (attributeValue.n() != null) {
            String numberStr = attributeValue.n();
            // Try to parse as integer first, then as double
            try {
                if (numberStr.contains(".")) {
                    return Double.parseDouble(numberStr);
                } else {
                    return Integer.parseInt(numberStr);
                }
            } catch (NumberFormatException e) {
                // Fallback to double
                return Double.parseDouble(numberStr);
            }
        }
        
        // Boolean
        if (attributeValue.bool() != null) {
            return attributeValue.bool();
        }
        
        // List
        if (attributeValue.hasL()) {
            return attributeValue.l().stream()
                .map(this::toJavaObject)
                .collect(Collectors.toList());
        }
        
        // Map (nested object)
        if (attributeValue.hasM()) {
            return convertAttributeValuesToMap(attributeValue.m());
        }
        
        // String Set
        if (attributeValue.hasSs()) {
            return attributeValue.ss();
        }
        
        // Number Set
        if (attributeValue.hasNs()) {
            return attributeValue.ns().stream()
                .map(Double::parseDouble)
                .collect(Collectors.toList());
        }
        
        LOG.warnf("Unsupported AttributeValue type: %s", attributeValue);
        return null;
    }
    
    /**
     * Extract String value from AttributeValue
     */
    public String getString(AttributeValue value) {
        return value != null ? value.s() : null;
    }
    
    /**
     * Extract Integer value from AttributeValue
     */
    public Integer getInteger(AttributeValue value) {
        if (value == null || value.n() == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.n());
        } catch (NumberFormatException e) {
            LOG.warnf("Could not parse integer from: %s", value.n());
            return null;
        }
    }
    
    /**
     * Extract Double value from AttributeValue
     */
    public Double getDouble(AttributeValue value) {
        if (value == null || value.n() == null) {
            return null;
        }
        try {
            return Double.parseDouble(value.n());
        } catch (NumberFormatException e) {
            LOG.warnf("Could not parse double from: %s", value.n());
            return null;
        }
    }
    
    /**
     * Extract Boolean value from AttributeValue
     */
    public Boolean getBoolean(AttributeValue value) {
        return value != null ? value.bool() : null;
    }
    
    /**
     * Extract List of Strings from AttributeValue
     */
    public List<String> getStringList(AttributeValue value) {
        if (value == null || !value.hasL()) {
            return null;
        }
        return value.l().stream()
            .map(AttributeValue::s)
            .collect(Collectors.toList());
    }
}
