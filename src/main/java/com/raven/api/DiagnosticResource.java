package com.raven.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Diagnostic endpoint to test AWS DynamoDB configuration
 */
@Path("/api/diagnostic")
public class DiagnosticResource {
    
    private static final Logger LOG = Logger.getLogger(DiagnosticResource.class);
    
    @Inject
    DynamoDbClient dynamoDbClient;
    
    @ConfigProperty(name = "quarkus.dynamodb.aws.region", defaultValue = "eu-central-1")
    String awsRegion;
    
    @ConfigProperty(name = "dynamodb.table.categories")
    String categoriesTable;
    
    @ConfigProperty(name = "dynamodb.table.products")
    String productsTable;
    
    @ConfigProperty(name = "dynamodb.table.orders")
    String ordersTable;
    
    /**
     * Test AWS credentials and DynamoDB connection
     */
    @GET
    @Path("/aws-config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkAwsConfiguration() {
        LOG.info("Checking AWS configuration");
        
        Map<String, Object> diagnosticInfo = new HashMap<>();
        
        try {
            // Check credentials
            AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
            AwsCredentials credentials = credentialsProvider.resolveCredentials();
            
            diagnosticInfo.put("status", "SUCCESS");
            diagnosticInfo.put("region", awsRegion);
            diagnosticInfo.put("accessKeyId", maskCredential(credentials.accessKeyId()));
            diagnosticInfo.put("credentialsType", credentials.getClass().getSimpleName());
            
            // Check environment variables
            Map<String, String> envVars = new HashMap<>();
            envVars.put("AWS_REGION", System.getenv("AWS_REGION"));
            envVars.put("AWS_DEFAULT_REGION", System.getenv("AWS_DEFAULT_REGION"));
            envVars.put("AWS_SHARED_CREDENTIALS_FILE", System.getenv("AWS_SHARED_CREDENTIALS_FILE"));
            envVars.put("AWS_PROFILE", System.getenv("AWS_PROFILE"));
            diagnosticInfo.put("environment", envVars);
            
            // Test DynamoDB connection
            try {
                ListTablesRequest request = ListTablesRequest.builder().limit(10).build();
                ListTablesResponse response = dynamoDbClient.listTables(request);
                
                diagnosticInfo.put("dynamoDbConnection", "SUCCESS");
                diagnosticInfo.put("tablesFound", response.tableNames());
                diagnosticInfo.put("tableCount", response.tableNames().size());
                
                // Check if required tables exist
                Map<String, Boolean> requiredTables = new HashMap<>();
                requiredTables.put(categoriesTable, response.tableNames().contains(categoriesTable));
                requiredTables.put(productsTable, response.tableNames().contains(productsTable));
                requiredTables.put(ordersTable, response.tableNames().contains(ordersTable));
                diagnosticInfo.put("requiredTables", requiredTables);
                
                boolean allTablesExist = requiredTables.values().stream().allMatch(exists -> exists);
                diagnosticInfo.put("allRequiredTablesExist", allTablesExist);
                
            } catch (Exception e) {
                LOG.error("Failed to connect to DynamoDB", e);
                diagnosticInfo.put("dynamoDbConnection", "FAILED");
                diagnosticInfo.put("dynamoDbError", e.getMessage());
                diagnosticInfo.put("errorType", e.getClass().getSimpleName());
            }
            
            return Response.ok(diagnosticInfo).build();
            
        } catch (Exception e) {
            LOG.error("Failed to check AWS configuration", e);
            diagnosticInfo.put("status", "FAILED");
            diagnosticInfo.put("error", e.getMessage());
            diagnosticInfo.put("errorType", e.getClass().getSimpleName());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(diagnosticInfo)
                .build();
        }
    }
    
    /**
     * Simple health check for DynamoDB
     */
    @GET
    @Path("/dynamodb-health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkDynamoDbHealth() {
        try {
            ListTablesRequest request = ListTablesRequest.builder().limit(1).build();
            dynamoDbClient.listTables(request);
            
            return Response.ok(Map.of(
                "status", "UP",
                "service", "DynamoDB",
                "region", awsRegion
            )).build();
            
        } catch (Exception e) {
            LOG.error("DynamoDB health check failed", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of(
                    "status", "DOWN",
                    "service", "DynamoDB",
                    "error", e.getMessage()
                ))
                .build();
        }
    }
    
    /**
     * Masks credential for logging (shows first 4 and last 4 characters)
     */
    private String maskCredential(String credential) {
        if (credential == null || credential.length() < 8) {
            return "****";
        }
        return credential.substring(0, 4) + "..." + credential.substring(credential.length() - 4);
    }
}
