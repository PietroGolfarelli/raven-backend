package com.raven.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;
import java.util.Optional;

/**
 * Custom AWS Client Producer to ensure proper DynamoDB client configuration
 * with explicit credential chain and region setup
 */
@ApplicationScoped
public class AwsClientProducer {
    
    private static final Logger LOG = Logger.getLogger(AwsClientProducer.class);
    
    @ConfigProperty(name = "quarkus.dynamodb.aws.region", defaultValue = "eu-central-1")
    String awsRegion;
    
    @ConfigProperty(name = "quarkus.dynamodb.endpoint-override")
    Optional<String> endpointOverride;
    
    @ConfigProperty(name = "aws.profile", defaultValue = "default")
    String awsProfile;
    
    /**
     * Produces a DynamoDB client with explicit configuration
     * This overrides the default Quarkus DynamoDB client to ensure proper setup
     */
    @Produces
    @ApplicationScoped
    public DynamoDbClient dynamoDbClient() {
        LOG.info("Creating custom DynamoDB client");
        
        try {
            // Create credentials provider chain
            AwsCredentialsProvider credentialsProvider = createCredentialsProvider();
            
            // Test credentials
            try {
                var credentials = credentialsProvider.resolveCredentials();
                LOG.infof("AWS Credentials resolved - Access Key ID: %s", 
                    maskCredential(credentials.accessKeyId()));
            } catch (Exception e) {
                LOG.error("Failed to resolve AWS credentials", e);
                throw new RuntimeException("AWS credentials not found or invalid", e);
            }
            
            // Build DynamoDB client
            DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider);
            
            // Add endpoint override if configured (for local development)
            if (endpointOverride.isPresent() && !endpointOverride.get().isBlank()) {
                String endpoint = endpointOverride.get();
                LOG.warnf("Using custom DynamoDB endpoint: %s (not recommended for production)", endpoint);
                builder.endpointOverride(URI.create(endpoint));
            } else {
                LOG.infof("Using AWS DynamoDB service endpoint for region: %s", awsRegion);
            }
            
            DynamoDbClient client = builder.build();
            
            LOG.infof("DynamoDB client created successfully - Region: %s", awsRegion);
            
            return client;
            
        } catch (Exception e) {
            LOG.error("Failed to create DynamoDB client", e);
            throw new RuntimeException("Failed to initialize DynamoDB client", e);
        }
    }
    
    /**
     * Creates a credentials provider chain that tries multiple sources in order:
     * 1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
     * 2. System properties
     * 3. AWS profile (default or specified)
     * 4. Default credential provider chain (includes credential_process, ECS, EC2, etc.)
     */
    private AwsCredentialsProvider createCredentialsProvider() {
        LOG.info("Setting up AWS credentials provider chain");
        
        // Use the default credentials provider which includes:
        // - Environment variables
        // - System properties
        // - Web identity token credentials
        // - Shared credentials file (~/.aws/credentials or AWS_SHARED_CREDENTIALS_FILE)
        // - ECS container credentials
        // - EC2 instance profile credentials
        AwsCredentialsProvider provider = DefaultCredentialsProvider.builder()
            .profileName(awsProfile)
            .build();
        
        LOG.infof("Credentials provider configured - Profile: %s", awsProfile);
        
        return provider;
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
