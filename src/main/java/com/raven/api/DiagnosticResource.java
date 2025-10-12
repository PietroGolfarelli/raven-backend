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
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
            
            // Test DynamoDB connection and check required tables
            // Using describeTable() instead of listTables() because:
            // 1. listTables() requires dynamodb:ListTables permission (global)
            // 2. describeTable() requires dynamodb:DescribeTable permission (table-specific)
            // 3. IAM policies often grant table-specific permissions but not global ListTables
            try {
                diagnosticInfo.put("dynamoDbConnection", "SUCCESS");
                
                // Check each required table using describeTable
                Map<String, Object> tableChecks = new HashMap<>();
                List<String> existingTables = new ArrayList<>();
                List<String> missingTables = new ArrayList<>();
                
                String[] requiredTableNames = {categoriesTable, productsTable, ordersTable};
                
                for (String tableName : requiredTableNames) {
                    try {
                        DescribeTableRequest request = DescribeTableRequest.builder()
                            .tableName(tableName)
                            .build();
                        
                        DescribeTableResponse response = dynamoDbClient.describeTable(request);
                        
                        Map<String, Object> tableInfo = new HashMap<>();
                        tableInfo.put("exists", true);
                        tableInfo.put("status", response.table().tableStatus().toString());
                        tableInfo.put("itemCount", response.table().itemCount());
                        tableInfo.put("tableSizeBytes", response.table().tableSizeBytes());
                        tableInfo.put("creationDateTime", response.table().creationDateTime().toString());
                        
                        tableChecks.put(tableName, tableInfo);
                        existingTables.add(tableName);
                        
                        LOG.infof("Table %s exists with status: %s", tableName, response.table().tableStatus());
                        
                    } catch (DynamoDbException e) {
                        Map<String, Object> tableInfo = new HashMap<>();
                        tableInfo.put("exists", false);
                        tableInfo.put("error", e.getMessage());
                        tableInfo.put("errorCode", e.awsErrorDetails() != null ? 
                            e.awsErrorDetails().errorCode() : "UNKNOWN");
                        
                        tableChecks.put(tableName, tableInfo);
                        missingTables.add(tableName);
                        
                        LOG.errorf("Table %s check failed: %s", tableName, e.getMessage());
                    }
                }
                
                diagnosticInfo.put("tableChecks", tableChecks);
                diagnosticInfo.put("existingTables", existingTables);
                diagnosticInfo.put("missingTables", missingTables);
                diagnosticInfo.put("tableCount", existingTables.size());
                diagnosticInfo.put("allRequiredTablesExist", missingTables.isEmpty());
                
                // Try listTables as well (may fail if permission not granted)
                try {
                    ListTablesRequest listRequest = ListTablesRequest.builder().limit(10).build();
                    ListTablesResponse listResponse = dynamoDbClient.listTables(listRequest);
                    diagnosticInfo.put("listTablesPermission", "GRANTED");
                    diagnosticInfo.put("allTablesInAccount", listResponse.tableNames());
                    LOG.info("ListTables permission is available");
                } catch (DynamoDbException e) {
                    diagnosticInfo.put("listTablesPermission", "DENIED");
                    diagnosticInfo.put("listTablesNote", 
                        "IAM user doesn't have dynamodb:ListTables permission. This is OK - table operations will still work.");
                    LOG.warnf("ListTables permission denied: %s. This is expected with limited IAM permissions.", e.getMessage());
                }
                
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
            // Try to describe one of our tables instead of listTables
            DescribeTableRequest request = DescribeTableRequest.builder()
                .tableName(categoriesTable)
                .build();
            dynamoDbClient.describeTable(request);
            
            return Response.ok(Map.of(
                "status", "UP",
                "service", "DynamoDB",
                "region", awsRegion,
                "testTable", categoriesTable
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
     * Test direct access to specific tables
     * This endpoint helps diagnose table access issues
     */
    @GET
    @Path("/test-table-access")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testTableAccess() {
        LOG.info("Testing direct table access");
        
        Map<String, Object> results = new HashMap<>();
        results.put("timestamp", java.time.Instant.now().toString());
        
        Map<String, Object> tableTests = new HashMap<>();
        
        // Test each table with describeTable and a sample scan
        String[] tables = {categoriesTable, productsTable, ordersTable};
        
        for (String tableName : tables) {
            Map<String, Object> tableResult = new HashMap<>();
            
            // Test 1: DescribeTable
            try {
                DescribeTableRequest descRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();
                DescribeTableResponse descResponse = dynamoDbClient.describeTable(descRequest);
                
                tableResult.put("describeTable", "SUCCESS");
                tableResult.put("tableStatus", descResponse.table().tableStatus().toString());
                tableResult.put("itemCount", descResponse.table().itemCount());
                
            } catch (DynamoDbException e) {
                tableResult.put("describeTable", "FAILED");
                tableResult.put("describeTableError", e.getMessage());
                tableResult.put("errorCode", e.awsErrorDetails() != null ? 
                    e.awsErrorDetails().errorCode() : "UNKNOWN");
            }
            
            // Test 2: Scan (limited to 1 item)
            try {
                software.amazon.awssdk.services.dynamodb.model.ScanRequest scanRequest = 
                    software.amazon.awssdk.services.dynamodb.model.ScanRequest.builder()
                        .tableName(tableName)
                        .limit(1)
                        .build();
                
                software.amazon.awssdk.services.dynamodb.model.ScanResponse scanResponse = 
                    dynamoDbClient.scan(scanRequest);
                
                tableResult.put("scanOperation", "SUCCESS");
                tableResult.put("scannedCount", scanResponse.count());
                tableResult.put("hasItems", scanResponse.count() > 0);
                
            } catch (DynamoDbException e) {
                tableResult.put("scanOperation", "FAILED");
                tableResult.put("scanError", e.getMessage());
                tableResult.put("scanErrorCode", e.awsErrorDetails() != null ? 
                    e.awsErrorDetails().errorCode() : "UNKNOWN");
                LOG.errorf("Scan failed for table %s: %s", tableName, e.getMessage());
            }
            
            tableTests.put(tableName, tableResult);
        }
        
        results.put("tableAccessTests", tableTests);
        
        // Summary
        long successfulTables = tableTests.values().stream()
            .filter(t -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> test = (Map<String, Object>) t;
                return "SUCCESS".equals(test.get("describeTable")) && 
                       "SUCCESS".equals(test.get("scanOperation"));
            })
            .count();
        
        results.put("successfulTables", successfulTables);
        results.put("totalTables", tables.length);
        results.put("allTablesAccessible", successfulTables == tables.length);
        
        return Response.ok(results).build();
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
