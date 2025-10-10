package com.raven.api.dynamo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@ApplicationScoped
public class DynamoConfig {
    @Produces
    DynamoDbEnhancedClient enhanced(DynamoDbClient base) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(base).build();
    }
}