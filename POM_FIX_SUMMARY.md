# POM.xml Fix Summary

## Date
October 12, 2025

## Overview
Fixed the `pom.xml` file in the raven-backend project to properly support DynamoDB integration with Quarkus, based on a working example from the budget-ls-server project.

## Changes Made

### 1. Updated Quarkus Version
- **Before:** `3.5.0`
- **After:** `3.15.1`
- **Reason:** Align with the working example and get latest bug fixes and features

### 2. Added quarkus-amazon-services-bom to dependencyManagement
```xml
<dependency>
    <groupId>io.quarkus.platform</groupId>
    <artifactId>quarkus-amazon-services-bom</artifactId>
    <version>3.15.1</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```
- **Reason:** This BOM (Bill of Materials) manages all AWS-related dependency versions consistently

### 3. Fixed DynamoDB Dependency
- **Before:** 
  ```xml
  <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-amazon-dynamodb</artifactId>
  </dependency>
  ```
- **After:**
  ```xml
  <dependency>
      <groupId>io.quarkiverse.amazonservices</groupId>
      <artifactId>quarkus-amazon-dynamodb</artifactId>
  </dependency>
  ```
- **Reason:** The correct groupId for Quarkus AWS extensions is `io.quarkiverse.amazonservices`, not `io.quarkus`. This was causing dependency resolution issues.

### 4. Added url-connection-client Dependency
```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>url-connection-client</artifactId>
</dependency>
```
- **Reason:** This is the AWS SDK HTTP client implementation required for DynamoDB operations. Without it, the application may fail at runtime when trying to connect to AWS services.

### 5. Updated Maven Plugin Versions
- **compiler-plugin:** `3.11.0` → `3.13.0`
- **surefire-plugin:** `3.1.2` → `3.3.1`
- **Reason:** Use more recent, stable versions of Maven plugins

### 6. Added Native Profile
Added a native build profile for GraalVM native compilation support:
```xml
<profile>
    <id>native</id>
    <activation>
        <property>
            <name>native</name>
        </property>
    </activation>
    ...
</profile>
```

## Files Updated
1. `/home/ubuntu/github_repos/raven-backend/pom.xml`
2. `/home/ubuntu/code_artifacts/quarkus-dynamodb-raven/pom.xml`

## Validation
✓ Both pom.xml files are well-formed XML
✓ Found 2 BOMs in dependencyManagement section
✓ DynamoDB dependency correctly uses `io.quarkiverse.amazonservices` groupId
✓ url-connection-client dependency added

## Key Benefits
1. **Proper Dependency Management:** The BOM approach ensures all AWS SDK and Quarkus dependencies use compatible versions
2. **No Version Conflicts:** Dependencies no longer specify explicit versions; they inherit from BOMs
3. **Runtime Reliability:** url-connection-client ensures AWS SDK can make HTTP calls
4. **Future-Proof:** Upgrading Quarkus version is now simpler (just change one property)

## Testing Recommendations
1. Run `./mvnw clean compile` to verify dependencies resolve correctly
2. Test DynamoDB operations to ensure they work as expected
3. Verify application starts without dependency errors
4. Test both development and production configurations

## References
- Working example: `/home/ubuntu/Uploads/user_message_2025-10-12_15-17-57.txt` (budget-ls-server project)
- Quarkus Amazon Services: https://quarkiverse.github.io/quarkiverse-docs/quarkus-amazon-services/dev/
- Quarkus BOM Documentation: https://quarkus.io/guides/platform
