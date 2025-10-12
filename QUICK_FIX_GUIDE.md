# Quick Fix Guide - DynamoDB Connection Issue

## 🚨 Problem
Your application cannot connect to DynamoDB because it's using Abacus AWS credentials that don't have DynamoDB permissions.

## ✅ Quick Solution (2 Steps)

### Step 1: Run the Setup Script

I've created a script that will automatically configure your AWS credentials:

```bash
cd /home/ubuntu/github_repos/raven-backend
./setup-aws-credentials.sh
```

The script will:
- ✅ Configure your AWS credentials
- ✅ Verify they work
- ✅ Check if DynamoDB tables exist
- ✅ Optionally create the tables for you

### Step 2: Start Your Application

```bash
./mvnw quarkus:dev
```

Then test it:
```bash
curl http://localhost:8080/api/products
```

---

## 📝 What You Need

Before running the setup script, make sure you have:

1. **Your AWS Access Key ID**
2. **Your AWS Secret Access Key**
3. **IAM permissions for DynamoDB** (PutItem, GetItem, UpdateItem, DeleteItem, Scan, Query)

### Where to Get AWS Credentials

1. Go to [AWS Console](https://console.aws.amazon.com/)
2. Navigate to **IAM** > **Users** > Your user
3. Go to **Security credentials** tab
4. Click **Create access key**
5. Save the Access Key ID and Secret Access Key

### Required IAM Permissions

Your AWS user needs these permissions:

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
        "dynamodb:Query",
        "dynamodb:CreateTable",
        "dynamodb:DescribeTable"
      ],
      "Resource": [
        "arn:aws:dynamodb:eu-central-1:*:table/raven-dev-*"
      ]
    }
  ]
}
```

---

## 🔍 What Was Wrong?

| Issue | Details |
|-------|---------|
| ❌ **Problem** | Application was using Abacus AWS credentials without DynamoDB access |
| ✅ **Configuration** | Table names are correctly configured as `raven-dev-categories`, `raven-dev-products`, `raven-dev-orders` |
| ✅ **Code** | Repository classes are correctly implemented |
| ⚠️ **Missing** | Your personal AWS credentials with DynamoDB permissions |

---

## 🆘 Alternative: Manual Configuration

If you prefer to configure manually instead of using the script:

### Option A: Environment Variables

```bash
export AWS_ACCESS_KEY_ID=your-access-key-id
export AWS_SECRET_ACCESS_KEY=your-secret-access-key
export AWS_REGION=eu-central-1
./mvnw quarkus:dev
```

### Option B: AWS Credentials File

1. Create directory: `mkdir -p ~/.aws`
2. Create `~/.aws/credentials`:
   ```ini
   [default]
   aws_access_key_id = your-access-key-id
   aws_secret_access_key = your-secret-access-key
   ```
3. Create `~/.aws/config`:
   ```ini
   [default]
   region = eu-central-1
   ```
4. Run: `./mvnw quarkus:dev`

---

## 📚 Need More Details?

See the full investigation report: [DYNAMODB_ISSUE_RESOLUTION.md](./DYNAMODB_ISSUE_RESOLUTION.md)

---

## 🎯 Summary

**Current Status**: Your application code is correct! ✅  
**Issue**: Missing AWS credentials with DynamoDB permissions ❌  
**Solution**: Run `./setup-aws-credentials.sh` and provide your AWS credentials ✅

---

*Date: October 12, 2025*
