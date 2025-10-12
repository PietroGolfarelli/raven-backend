
package com.raven.util;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for managing DynamoDB keys generation and validation
 */
@ApplicationScoped
public class DynamoDBKeyManager {
    
    private static final Logger LOG = Logger.getLogger(DynamoDBKeyManager.class);
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    
    /**
     * Generate a new unique ID using UUID v4
     * 
     * @return A new UUID string
     */
    public String generateId() {
        String id = UUID.randomUUID().toString();
        LOG.debugf("Generated new ID: %s", id);
        return id;
    }
    
    /**
     * Generate a new unique ID with a prefix
     * 
     * @param prefix The prefix to add to the ID
     * @return A new prefixed UUID string
     */
    public String generateId(String prefix) {
        String id = prefix + UUID.randomUUID().toString();
        LOG.debugf("Generated new ID with prefix: %s", id);
        return id;
    }
    
    /**
     * Validate if a string is a valid UUID
     * 
     * @param id The ID to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidId(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        return UUID_PATTERN.matcher(id).matches();
    }
    
    /**
     * Validate and throw exception if invalid
     * 
     * @param id The ID to validate
     * @param fieldName The name of the field for error message
     * @throws IllegalArgumentException if ID is invalid
     */
    public void validateId(String id, String fieldName) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException(
                String.format("Invalid %s: '%s'. Must be a valid UUID.", fieldName, id)
            );
        }
    }
    
    /**
     * Generate or validate an ID - if null, generate new, otherwise validate
     * 
     * @param id The ID to validate or null to generate
     * @param fieldName The name of the field for error message
     * @return The validated or newly generated ID
     */
    public String ensureValidId(String id, String fieldName) {
        if (id == null || id.isBlank()) {
            return generateId();
        }
        validateId(id, fieldName);
        return id;
    }
}
