# Quick Testing Guide - DynamoDB Table Access Fix

## What Was Fixed

The application now properly detects and accesses DynamoDB tables even when the IAM user doesn't have global `dynamodb:ListTables` permission. The diagnostic endpoints now use `describeTable()` which requires only table-specific permissions.

## Testing Steps

### 1. Start Your Application

```bash
cd /tmp/code_artifacts/raven-backend
./mvnw quarkus:dev
```

Wait for the application to start (you should see "Listening on: http://0.0.0.0:8080")

### 2. Test the Updated Diagnostic Endpoint

```bash
curl http://localhost:8080/api/diagnostic/aws-config | jq
```

**What to look for:**
- `"allRequiredTablesExist": true`
- `"existingTables"` should show all three tables
- Each table in `"tableChecks"` should have `"exists": true`
- `"listTablesPermission"` might say `"DENIED"` - **this is OK!**

### 3. Test the New Table Access Endpoint

```bash
curl http://localhost:8080/api/diagnostic/test-table-access | jq
```

**What to look for:**
- `"allTablesAccessible": true`
- Each table should show:
  - `"describeTable": "SUCCESS"`
  - `"scanOperation": "SUCCESS"`
  - `"tableStatus": "ACTIVE"`

### 4. Test the Categories API

```bash
curl http://localhost:8080/api/categories | jq
```

**Expected results:**
- Should return a JSON array of categories
- If empty array `[]`, that's fine - it means the table is accessible but has no data
- Should NOT return `"ResourceNotFoundException"` error

### 5. Test the Health Check

```bash
curl http://localhost:8080/api/diagnostic/dynamodb-health | jq
```

**What to look for:**
- `"status": "UP"`
- `"service": "DynamoDB"`

## Understanding the Results

### ✅ Success Indicators

1. **Diagnostic endpoint shows tables exist:**
   ```json
   {
     "allRequiredTablesExist": true,
     "existingTables": ["raven-dev-categories", "raven-dev-products", "raven-dev-orders"],
     "tableCount": 3
   }
   ```

2. **Table access test succeeds:**
   ```json
   {
     "allTablesAccessible": true,
     "successfulTables": 3
   }
   ```

3. **Categories API returns data or empty array:**
   ```json
   []  // or actual data
   ```

### ⚠️ Expected Warnings (These are OK!)

1. **ListTables permission denied:**
   ```json
   {
     "listTablesPermission": "DENIED",
     "listTablesNote": "IAM user doesn't have dynamodb:ListTables permission. This is OK - table operations will still work."
   }
   ```
   
   **This is normal!** Your IAM user has table-specific permissions, which is the recommended security practice.

### ❌ Problems to Fix

1. **describeTable fails for a table:**
   ```json
   {
     "describeTable": "FAILED",
     "errorCode": "ResourceNotFoundException"
   }
   ```
   
   **Solution:** Check that the table name in `application.properties` matches the actual DynamoDB table name.

2. **Scan operation fails:**
   ```json
   {
     "scanOperation": "FAILED",
     "scanErrorCode": "AccessDeniedException"
   }
   ```
   
   **Solution:** Your IAM user needs `dynamodb:Scan` permission on the table.

## Adding Sample Data (Optional)

If your tables are empty and you want to test with data, you can add a category via the API:

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Category",
    "description": "A test category for validation"
  }'
```

Then retrieve it:

```bash
curl http://localhost:8080/api/categories | jq
```

## Viewing Application Logs

To see detailed logging about table access:

```bash
# If running in dev mode, logs appear in the console
# Look for these log messages:

# 1. Table scan attempt:
"Attempting to scan table: raven-dev-categories"

# 2. Successful scan:
"Scan successful for table: raven-dev-categories, scanned count: 5"

# 3. Table found:
"Found 5 categories from table: raven-dev-categories"
```

## Troubleshooting

### Issue: "Cannot do operations on a non-existent table"

**Possible causes:**
1. Table name mismatch in `application.properties`
2. Wrong AWS region
3. Missing IAM permissions

**Debug steps:**
1. Check application logs for the exact table name being used
2. Verify table exists: `aws dynamodb describe-table --table-name raven-dev-categories --region eu-central-1`
3. Check IAM permissions with the test endpoint: `/api/diagnostic/test-table-access`

### Issue: "AccessDeniedException"

**Solution:** Your IAM user needs these permissions:
- `dynamodb:DescribeTable`
- `dynamodb:Scan` (for listing items)
- `dynamodb:Query` (for querying)
- `dynamodb:GetItem` (for retrieving specific items)
- `dynamodb:PutItem` (for creating items)
- `dynamodb:UpdateItem` (for updating items)
- `dynamodb:DeleteItem` (for deleting items)

### Issue: Empty tables array but tables exist

**This is fixed!** The new code uses `describeTable()` instead of `listTables()`, so this issue should no longer occur.

## Next Steps After Successful Testing

1. ✅ Verify all three tables are accessible
2. ✅ Test CRUD operations on categories
3. ✅ Test products and orders APIs
4. ✅ Review and update IAM permissions if needed
5. ✅ Add application data

## Need More Information?

See the comprehensive fix documentation: [DYNAMODB_TABLE_ACCESS_FIX.md](./DYNAMODB_TABLE_ACCESS_FIX.md)

## Summary

The fix changes how the application checks for table existence:
- **Before:** Used `listTables()` - requires global permission ❌
- **After:** Uses `describeTable()` - requires only table-specific permission ✅

Your application should now work correctly with table-specific IAM permissions, which is the recommended security practice.
