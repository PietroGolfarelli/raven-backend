# DynamoDB Table Configuration Issue - Resolution

## 🔍 Issue Summary

When calling `http://localhost:8080/api/products`, the application returns a `ResourceNotFoundException` error:
```
Cannot do operations on a non-existent table (Service: DynamoDb, Status Code: 400, Request ID: aa012197-1f72-4f6f-bd07-b20824a90a3a)
```

## 🕵️ Investigation Results

### 1. ✅ Application Configuration (CORRECT)

The `application.properties` file is **correctly configured** with the right table names:

```properties
# DynamoDB Table Names
dynamodb.table.categories=raven-dev-categories
dynamodb.table.products=raven-dev-products
dynamodb.table.orders=raven-dev-orders

# DynamoDB GSI Names
dynamodb.gsi.products-by-category=products_by_category

# AWS Region
quarkus.dynamodb.aws.region=eu-central-1
```

### 2. ✅ Repository Classes (CORRECT)

All repository classes (`ProductRepository`, `CategoryRepository`, `OrderRepository`) are correctly using the `@ConfigProperty` annotation to read table names from configuration:

```java
@ConfigProperty(name = "dynamodb.table.products")
String tableName;
```

### 3. ❌ AWS Credentials Issue (ROOT CAUSE)

The application is running with **Abacus AWS credentials** which **DO NOT have DynamoDB permissions**. When I tested the credentials:

```bash
aws dynamodb describe-table --table-name raven-dev-products --region eu-central-1

# Result: AccessDeniedException
# User: arn:aws:sts::448970459817:assumed-role/spark-permissions/...
# is not authorized to perform: dynamodb:DescribeTable
```

This is the **root cause** of the issue. The application needs to use **YOUR AWS credentials** that have access to your DynamoDB tables.

## 🔧 Solution

You need to configure your own AWS credentials that have access to your DynamoDB tables (`raven-dev-categories`, `raven-dev-orders`, `raven-dev-products`).

### Option 1: Using Environment Variables (Recommended for Local Development)

1. **Create a `.env` file** in the project root:
   ```bash
   cd /home/ubuntu/github_repos/raven-backend
   cp .env.example .env
   ```

2. **Edit the `.env` file** with your AWS credentials:
   ```bash
   AWS_ACCESS_KEY_ID=your-actual-access-key-id
   AWS_SECRET_ACCESS_KEY=your-actual-secret-access-key
   AWS_REGION=eu-central-1
   ```

3. **Export the environment variables** before running the application:
   ```bash
   export AWS_ACCESS_KEY_ID=your-actual-access-key-id
   export AWS_SECRET_ACCESS_KEY=your-actual-secret-access-key
   export AWS_REGION=eu-central-1
   ```

4. **Run the application**:
   ```bash
   ./mvnw quarkus:dev
   ```

### Option 2: Using AWS Credentials File

1. **Create AWS credentials directory**:
   ```bash
   mkdir -p ~/.aws
   ```

2. **Create credentials file** (`~/.aws/credentials`):
   ```ini
   [default]
   aws_access_key_id = your-actual-access-key-id
   aws_secret_access_key = your-actual-secret-access-key
   ```

3. **Create config file** (`~/.aws/config`):
   ```ini
   [default]
   region = eu-central-1
   ```

4. **Run the application**:
   ```bash
   ./mvnw quarkus:dev
   ```

### Option 3: Using Static Credentials in application.properties (Not Recommended for Production)

1. **Edit `src/main/resources/application.properties`**:
   ```properties
   # Uncomment and configure with your credentials
   quarkus.dynamodb.aws.credentials.type=static
   quarkus.dynamodb.aws.credentials.static-provider.access-key-id=your-actual-access-key-id
   quarkus.dynamodb.aws.credentials.static-provider.secret-access-key=your-actual-secret-access-key
   ```

⚠️ **Warning**: Never commit credentials to version control!

## 🔑 Required IAM Permissions

Your AWS user/role must have the following DynamoDB permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:UpdateItem",
        "dynamodb:DeleteItem",
        "dynamodb:Scan",
        "dynamodb:Query"
      ],
      "Resource": [
        "arn:aws:dynamodb:eu-central-1:YOUR-ACCOUNT-ID:table/raven-dev-categories",
        "arn:aws:dynamodb:eu-central-1:YOUR-ACCOUNT-ID:table/raven-dev-products",
        "arn:aws:dynamodb:eu-central-1:YOUR-ACCOUNT-ID:table/raven-dev-orders",
        "arn:aws:dynamodb:eu-central-1:YOUR-ACCOUNT-ID:table/raven-dev-products/index/products_by_category"
      ]
    }
  ]
}
```

## ✅ Verification Steps

After configuring your credentials, verify the setup:

1. **Test AWS credentials**:
   ```bash
   aws sts get-caller-identity
   ```
   This should show your AWS account information.

2. **Test DynamoDB access**:
   ```bash
   aws dynamodb describe-table --table-name raven-dev-products --region eu-central-1
   ```
   This should return the table description without errors.

3. **Test the application**:
   ```bash
   # Start the application
   ./mvnw quarkus:dev
   
   # In another terminal, test the endpoint
   curl http://localhost:8080/api/products
   ```

## 📋 Summary of Findings

| Component | Status | Details |
|-----------|--------|---------|
| **application.properties** | ✅ CORRECT | Table names match: `raven-dev-categories`, `raven-dev-products`, `raven-dev-orders` |
| **Repository Classes** | ✅ CORRECT | Using `@ConfigProperty` to read table names from configuration |
| **AWS Region** | ✅ CORRECT | Configured as `eu-central-1` |
| **AWS Credentials** | ❌ ISSUE | Using Abacus credentials without DynamoDB permissions |
| **DynamoDB Tables** | ⚠️ UNKNOWN | Need to verify existence with your credentials |

## 🎯 Action Items for You

1. **Get your AWS credentials** that have access to DynamoDB tables
2. **Configure credentials** using one of the options above (Option 1 recommended)
3. **Verify** your credentials have the required IAM permissions
4. **Restart** the application with your credentials
5. **Test** the API endpoints

## 🔗 Additional Resources

- [AWS Credentials Configuration](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html)
- [DynamoDB IAM Policies](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/security-iam.html)
- [Quarkus DynamoDB Extension](https://quarkus.io/guides/dynamodb)

---

**Date**: October 12, 2025  
**Status**: Awaiting user to configure their AWS credentials
