# DynamoDB Table Access Fix - Summary

## Problem Overview

The application was unable to see or access DynamoDB tables even though:
- The connection to DynamoDB was successful
- AWS credentials were correctly configured
- The tables existed and were accessible via AWS CLI

### Symptoms
1. The diagnostic endpoint `/api/diagnostic/aws-config` showed:
   - `dynamoDbConnection: SUCCESS`
   - `tableCount: 0`
   - `tablesFound: []`
   - `allRequiredTablesExist: false`

2. The `/api/categories` endpoint threw:
   ```
   ResourceNotFoundException: Cannot do operations on a non-existent table
   ```

## Root Cause Analysis

The issue was caused by **IAM permission limitations**, specifically:

### The `listTables()` Permission Issue

1. **`listTables()` requires global permission**: The DynamoDB `listTables()` API call requires the `dynamodb:ListTables` IAM permission, which is a **global account-level permission**.

2. **Table-specific permissions are more common**: Many IAM policies grant table-specific permissions like:
   - `dynamodb:Scan`
   - `dynamodb:Query`
   - `dynamodb:GetItem`
   - `dynamodb:PutItem`
   - `dynamodb:DescribeTable`
   
   But they don't include the global `dynamodb:ListTables` permission.

3. **Misleading diagnostic**: The original diagnostic endpoint used `listTables()` to check if tables exist. When this call failed or returned empty results, it incorrectly suggested that tables don't exist, even though they were accessible.

## Solution Implemented

### 1. Updated DiagnosticResource to use `describeTable()` instead of `listTables()`

**Why this works:**
- `describeTable()` requires only `dynamodb:DescribeTable` permission, which is table-specific
- This permission is more commonly granted in IAM policies
- It can verify table existence without requiring global ListTables permission

**Changes made to `/src/main/java/com/raven/api/DiagnosticResource.java`:**

```java
// OLD: Used listTables() which requires global permission
ListTablesRequest request = ListTablesRequest.builder().limit(10).build();
ListTablesResponse response = dynamoDbClient.listTables(request);
diagnosticInfo.put("tablesFound", response.tableNames());

// NEW: Uses describeTable() for each required table
for (String tableName : requiredTableNames) {
    DescribeTableRequest request = DescribeTableRequest.builder()
        .tableName(tableName)
        .build();
    DescribeTableResponse response = dynamoDbClient.describeTable(request);
    // Extract detailed table information
}
```

### 2. Added New Diagnostic Endpoint: `/api/diagnostic/test-table-access`

This endpoint tests:
- **DescribeTable**: Verifies table exists and retrieves metadata
- **Scan**: Tests actual data access with a limit of 1 item

**Benefits:**
- Provides detailed diagnostics for each table
- Shows which specific permissions are granted
- Helps identify permission issues quickly

**Example response:**
```json
{
  "timestamp": "2025-10-12T21:30:00Z",
  "tableAccessTests": {
    "raven-dev-categories": {
      "describeTable": "SUCCESS",
      "tableStatus": "ACTIVE",
      "itemCount": 5,
      "scanOperation": "SUCCESS",
      "scannedCount": 1,
      "hasItems": true
    },
    "raven-dev-products": { ... },
    "raven-dev-orders": { ... }
  },
  "successfulTables": 3,
  "totalTables": 3,
  "allTablesAccessible": true
}
```

### 3. Enhanced Logging in CategoryRepository

Added detailed logging to help diagnose issues:

```java
LOG.infof("Attempting to scan table: %s", tableName);
// ... operation ...
LOG.infof("Scan successful for table: %s, scanned count: %d", tableName, response.count());
```

**Benefits:**
- Shows exactly which table name is being used
- Logs DynamoDB error codes for better debugging
- Helps identify configuration issues quickly

### 4. Updated Health Check Endpoint

Changed `/api/diagnostic/dynamodb-health` to use `describeTable()` instead of `listTables()`:

```java
// Uses describeTable on a known table instead of listTables
DescribeTableRequest request = DescribeTableRequest.builder()
    .tableName(categoriesTable)
    .build();
dynamoDbClient.describeTable(request);
```

## Files Modified

1. **`src/main/java/com/raven/api/DiagnosticResource.java`**
   - Updated `checkAwsConfiguration()` to use `describeTable()`
   - Added `testTableAccess()` endpoint
   - Updated `checkDynamoDbHealth()` to use `describeTable()`

2. **`src/main/java/com/raven/repository/CategoryRepository.java`**
   - Enhanced logging in `findAll()` method
   - Added error code logging for DynamoDB exceptions

## Testing the Fix

### 1. Test the Diagnostic Endpoint
```bash
curl http://localhost:8080/api/diagnostic/aws-config
```

**Expected result:**
- `allRequiredTablesExist: true`
- `existingTables: ["raven-dev-categories", "raven-dev-products", "raven-dev-orders"]`
- `tableChecks` showing each table with `exists: true` and status information

### 2. Test Table Access
```bash
curl http://localhost:8080/api/diagnostic/test-table-access
```

**Expected result:**
- `allTablesAccessible: true`
- Each table showing `describeTable: SUCCESS` and `scanOperation: SUCCESS`

### 3. Test Categories API
```bash
curl http://localhost:8080/api/categories
```

**Expected result:**
- Should return the list of categories (or empty array if no data exists)
- No more `ResourceNotFoundException`

## IAM Permissions Summary

### Required Permissions for Full Functionality

The application now works with these **minimum table-specific permissions**:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:DescribeTable",
        "dynamodb:Scan",
        "dynamodb:Query",
        "dynamodb:GetItem",
        "dynamodb:PutItem",
        "dynamodb:UpdateItem",
        "dynamodb:DeleteItem"
      ],
      "Resource": [
        "arn:aws:dynamodb:eu-central-1:019714370370:table/raven-dev-categories",
        "arn:aws:dynamodb:eu-central-1:019714370370:table/raven-dev-products",
        "arn:aws:dynamodb:eu-central-1:019714370370:table/raven-dev-orders"
      ]
    }
  ]
}
```

### Optional Permission (for the diagnostic listTables check)

```json
{
  "Effect": "Allow",
  "Action": "dynamodb:ListTables",
  "Resource": "*"
}
```

**Note:** The `ListTables` permission is **NOT required** for the application to function. It's only useful for the diagnostic endpoint to show all tables in the account.

## Configuration

The table names are configured in `src/main/resources/application.properties`:

```properties
# DynamoDB Table Names
dynamodb.table.categories=raven-dev-categories
dynamodb.table.products=raven-dev-products
dynamodb.table.orders=raven-dev-orders
```

Make sure these match your actual DynamoDB table names.

## Verification Steps

After deploying the fix:

1. **Check application logs** for table name being used:
   ```
   INFO  [com.raven.repository.CategoryRepository] Attempting to scan table: raven-dev-categories
   INFO  [com.raven.repository.CategoryRepository] Scan successful for table: raven-dev-categories, scanned count: 5
   ```

2. **Test diagnostic endpoints**:
   - `/api/diagnostic/aws-config` - Should show all tables exist
   - `/api/diagnostic/test-table-access` - Should show successful access to all tables
   - `/api/diagnostic/dynamodb-health` - Should return status UP

3. **Test API endpoints**:
   - `/api/categories` - Should return categories
   - `/api/products` - Should return products
   - `/api/orders` - Should return orders

## Key Takeaways

1. **Don't rely on `listTables()` for table existence checks** - Use `describeTable()` instead
2. **IAM permissions matter** - Table-specific permissions are more common than global ListTables
3. **Diagnostic endpoints should test actual operations** - Not just connection status
4. **Detailed logging is essential** - Log table names and error codes to help debug issues

## Next Steps

1. **Deploy the updated code** to your environment
2. **Test the diagnostic endpoints** to verify table access
3. **Test the API endpoints** to ensure data operations work
4. **Review IAM permissions** if any issues persist
5. **Add sample data** to tables if they're empty

## Need More Help?

If you still experience issues:

1. Check the application logs for specific error messages
2. Use the `/api/diagnostic/test-table-access` endpoint to identify which operations fail
3. Verify your IAM user has the required permissions listed above
4. Ensure the table names in `application.properties` match your DynamoDB tables exactly
