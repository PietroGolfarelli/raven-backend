#!/bin/bash

# Setup AWS Credentials for Raven Backend
# This script helps you configure AWS credentials for local development

set -e

echo "=========================================="
echo "  Raven Backend - AWS Credentials Setup"
echo "=========================================="
echo ""

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "⚠️  AWS CLI is not installed. Installing..."
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
    unzip -q awscliv2.zip
    sudo ./aws/install
    rm -rf aws awscliv2.zip
    echo "✅ AWS CLI installed successfully"
fi

echo "Please enter your AWS credentials:"
echo ""

# Get AWS Access Key ID
read -p "AWS Access Key ID: " aws_access_key_id
if [ -z "$aws_access_key_id" ]; then
    echo "❌ AWS Access Key ID is required"
    exit 1
fi

# Get AWS Secret Access Key
read -sp "AWS Secret Access Key: " aws_secret_access_key
echo ""
if [ -z "$aws_secret_access_key" ]; then
    echo "❌ AWS Secret Access Key is required"
    exit 1
fi

# Get AWS Region (default: eu-central-1)
read -p "AWS Region [eu-central-1]: " aws_region
aws_region=${aws_region:-eu-central-1}

echo ""
echo "Setting up credentials..."

# Create .aws directory if it doesn't exist
mkdir -p ~/.aws

# Create credentials file
cat > ~/.aws/credentials << EOF
[default]
aws_access_key_id = $aws_access_key_id
aws_secret_access_key = $aws_secret_access_key
EOF

echo "✅ Created ~/.aws/credentials"

# Create config file
cat > ~/.aws/config << EOF
[default]
region = $aws_region
output = json
EOF

echo "✅ Created ~/.aws/config"

# Set permissions
chmod 600 ~/.aws/credentials
chmod 600 ~/.aws/config

echo ""
echo "=========================================="
echo "  Verifying AWS Credentials"
echo "=========================================="
echo ""

# Test AWS credentials
if aws sts get-caller-identity &> /dev/null; then
    echo "✅ AWS credentials are valid!"
    echo ""
    aws sts get-caller-identity
else
    echo "❌ Failed to verify AWS credentials"
    echo "   Please check your credentials and try again"
    exit 1
fi

echo ""
echo "=========================================="
echo "  Verifying DynamoDB Access"
echo "=========================================="
echo ""

# Check if DynamoDB tables exist
tables=("raven-dev-categories" "raven-dev-products" "raven-dev-orders")
all_tables_exist=true

for table in "${tables[@]}"; do
    echo -n "Checking table: $table... "
    if aws dynamodb describe-table --table-name "$table" --region "$aws_region" &> /dev/null; then
        echo "✅ EXISTS"
    else
        echo "❌ NOT FOUND or NO ACCESS"
        all_tables_exist=false
    fi
done

echo ""

if [ "$all_tables_exist" = false ]; then
    echo "⚠️  Some tables are missing or you don't have access to them."
    echo "   Please ensure:"
    echo "   1. The tables exist in region: $aws_region"
    echo "   2. Your IAM user/role has the required DynamoDB permissions"
    echo ""
    echo "   Required permissions: PutItem, GetItem, UpdateItem, DeleteItem, Scan, Query"
    echo ""
    read -p "Do you want to create the tables? (y/N): " create_tables
    if [ "$create_tables" = "y" ] || [ "$create_tables" = "Y" ]; then
        echo ""
        echo "Creating DynamoDB tables..."
        
        # Create categories table
        echo "Creating raven-dev-categories..."
        aws dynamodb create-table \
            --table-name raven-dev-categories \
            --attribute-definitions AttributeName=id,AttributeType=S \
            --key-schema AttributeName=id,KeyType=HASH \
            --billing-mode PAY_PER_REQUEST \
            --region "$aws_region" \
            &> /dev/null && echo "✅ Created raven-dev-categories" || echo "❌ Failed to create raven-dev-categories"
        
        # Create products table with GSI
        echo "Creating raven-dev-products..."
        aws dynamodb create-table \
            --table-name raven-dev-products \
            --attribute-definitions \
                AttributeName=id,AttributeType=S \
                AttributeName=categoryId,AttributeType=S \
            --key-schema AttributeName=id,KeyType=HASH \
            --global-secondary-indexes \
                "IndexName=products_by_category,KeySchema=[{AttributeName=categoryId,KeyType=HASH}],Projection={ProjectionType=ALL},ProvisionedThroughput={ReadCapacityUnits=5,WriteCapacityUnits=5}" \
            --billing-mode PROVISIONED \
            --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
            --region "$aws_region" \
            &> /dev/null && echo "✅ Created raven-dev-products" || echo "❌ Failed to create raven-dev-products"
        
        # Create orders table
        echo "Creating raven-dev-orders..."
        aws dynamodb create-table \
            --table-name raven-dev-orders \
            --attribute-definitions AttributeName=id,AttributeType=S \
            --key-schema AttributeName=id,KeyType=HASH \
            --billing-mode PAY_PER_REQUEST \
            --region "$aws_region" \
            &> /dev/null && echo "✅ Created raven-dev-orders" || echo "❌ Failed to create raven-dev-orders"
        
        echo ""
        echo "⏳ Waiting for tables to become active (this may take a minute)..."
        sleep 10
        
        for table in "${tables[@]}"; do
            aws dynamodb wait table-exists --table-name "$table" --region "$aws_region" 2>/dev/null && \
                echo "✅ Table $table is active" || \
                echo "⚠️  Table $table is still being created"
        done
    fi
else
    echo "✅ All DynamoDB tables are accessible!"
fi

echo ""
echo "=========================================="
echo "  Setup Complete!"
echo "=========================================="
echo ""
echo "You can now run the application:"
echo ""
echo "  cd /home/ubuntu/github_repos/raven-backend"
echo "  ./mvnw quarkus:dev"
echo ""
echo "Then test the API:"
echo ""
echo "  curl http://localhost:8080/api/products"
echo "  curl http://localhost:8080/api/categories"
echo "  curl http://localhost:8080/api/orders"
echo ""
echo "API Documentation: http://localhost:8080/swagger-ui"
echo ""
