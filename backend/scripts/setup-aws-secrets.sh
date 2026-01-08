#!/bin/bash

# AWS Secrets Manager Setup Script for E-commerce Backend
# This script creates the necessary secrets in AWS Secrets Manager

set -e

# Configuration
SECRET_NAME="ecommerce/db/credentials"
REGION="us-east-1"
DB_HOST="ecommerce-mysql-db.cwhegcq847i3.us-east-1.rds.amazonaws.com"
DB_PORT="3306"
DB_NAME="ecommerce"
DB_USERNAME="admin"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Setting up AWS Secrets Manager for E-commerce Backend${NC}"
echo "=================================================="

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI is not installed. Please install it first.${NC}"
    exit 1
fi

# Check if AWS credentials are configured
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}Error: AWS credentials are not configured. Please run 'aws configure' first.${NC}"
    exit 1
fi

echo -e "${YELLOW}Current AWS Identity:${NC}"
aws sts get-caller-identity

# Prompt for database password
echo ""
read -s -p "Enter the database password for user '$DB_USERNAME': " DB_PASSWORD
echo ""

if [ -z "$DB_PASSWORD" ]; then
    echo -e "${RED}Error: Database password cannot be empty.${NC}"
    exit 1
fi

# Create the secret JSON
SECRET_VALUE=$(cat <<EOF
{
  "username": "$DB_USERNAME",
  "password": "$DB_PASSWORD",
  "engine": "mysql",
  "host": "$DB_HOST",
  "port": $DB_PORT,
  "dbname": "$DB_NAME",
  "dbInstanceIdentifier": "ecommerce-mysql-db"
}
EOF
)

echo -e "${YELLOW}Creating secret in AWS Secrets Manager...${NC}"

# Check if secret already exists
if aws secretsmanager describe-secret --secret-id "$SECRET_NAME" --region "$REGION" &> /dev/null; then
    echo -e "${YELLOW}Secret already exists. Updating...${NC}"
    aws secretsmanager update-secret \
        --secret-id "$SECRET_NAME" \
        --secret-string "$SECRET_VALUE" \
        --region "$REGION"
    echo -e "${GREEN}Secret updated successfully!${NC}"
else
    echo -e "${YELLOW}Creating new secret...${NC}"
    aws secretsmanager create-secret \
        --name "$SECRET_NAME" \
        --description "Database credentials for E-commerce application" \
        --secret-string "$SECRET_VALUE" \
        --region "$REGION"
    echo -e "${GREEN}Secret created successfully!${NC}"
fi

# Create additional secrets for JWT and other configurations
JWT_SECRET_NAME="ecommerce/app/jwt-secret"
JWT_SECRET_VALUE=$(openssl rand -base64 32)

echo -e "${YELLOW}Creating JWT secret...${NC}"
if aws secretsmanager describe-secret --secret-id "$JWT_SECRET_NAME" --region "$REGION" &> /dev/null; then
    echo -e "${YELLOW}JWT secret already exists. Updating...${NC}"
    aws secretsmanager update-secret \
        --secret-id "$JWT_SECRET_NAME" \
        --secret-string "$JWT_SECRET_VALUE" \
        --region "$REGION"
else
    aws secretsmanager create-secret \
        --name "$JWT_SECRET_NAME" \
        --description "JWT secret key for E-commerce application" \
        --secret-string "$JWT_SECRET_VALUE" \
        --region "$REGION"
fi

echo -e "${GREEN}JWT secret configured successfully!${NC}"

# Display the secrets
echo ""
echo -e "${GREEN}Secrets created/updated:${NC}"
echo "1. Database credentials: $SECRET_NAME"
echo "2. JWT secret: $JWT_SECRET_NAME"

echo ""
echo -e "${YELLOW}To use these secrets in your application:${NC}"
echo "1. Ensure your ECS task role has the following permissions:"
echo "   - secretsmanager:GetSecretValue"
echo "   - secretsmanager:DescribeSecret"
echo ""
echo "2. Set the following environment variables in your ECS task definition:"
echo "   - AWS_SECRETS_ENABLED=true"
echo "   - DB_SECRET_NAME=$SECRET_NAME"
echo "   - JWT_SECRET_NAME=$JWT_SECRET_NAME"
echo ""
echo -e "${GREEN}Setup completed successfully!${NC}"

# Create IAM policy document for reference
cat > iam-secrets-policy.json <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue",
                "secretsmanager:DescribeSecret"
            ],
            "Resource": [
                "arn:aws:secretsmanager:$REGION:*:secret:$SECRET_NAME*",
                "arn:aws:secretsmanager:$REGION:*:secret:$JWT_SECRET_NAME*"
            ]
        }
    ]
}
EOF

echo -e "${YELLOW}IAM policy document created: iam-secrets-policy.json${NC}"
echo "Attach this policy to your ECS task role."