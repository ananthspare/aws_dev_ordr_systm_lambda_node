# AWS Configuration Guide for E-commerce Backend

This guide explains how to configure the Spring Boot backend to work with AWS services including RDS, Secrets Manager, S3, and ECS.

## Table of Contents
1. [Configuration Files](#configuration-files)
2. [AWS Secrets Manager Setup](#aws-secrets-manager-setup)
3. [Environment Variables](#environment-variables)
4. [Local Testing with AWS](#local-testing-with-aws)
5. [ECS Deployment](#ecs-deployment)
6. [IAM Permissions](#iam-permissions)
7. [Troubleshooting](#troubleshooting)

## Configuration Files

### 1. application-aws.yml
The main AWS configuration file located at `src/main/resources/application-aws.yml`. This file contains:
- Database connection settings with AWS Secrets Manager integration
- AWS service configurations (S3, DynamoDB, SQS, EventBridge)
- Security settings
- Logging configuration
- Application-specific settings

### 2. aws-environment-variables.env
Template file containing all environment variables needed for AWS deployment. Copy and modify this file for your specific environment.

### 3. docker-compose-aws.yml
Docker Compose configuration for testing AWS integration locally using LocalStack.

## AWS Secrets Manager Setup

### Prerequisites
- AWS CLI installed and configured
- Appropriate IAM permissions for Secrets Manager

### Automatic Setup (Recommended)

#### For Linux/Mac:
```bash
cd backend/scripts
chmod +x setup-aws-secrets.sh
./setup-aws-secrets.sh
```

#### For Windows:
```powershell
cd backend/scripts
.\setup-aws-secrets.ps1
```

### Manual Setup
1. Create database credentials secret:
```bash
aws secretsmanager create-secret \
    --name "ecommerce/db/credentials" \
    --description "Database credentials for E-commerce application" \
    --secret-string '{
        "username": "admin",
        "password": "your-db-password",
        "engine": "mysql",
        "host": "ecommerce-mysql-db.cwhegcq847i3.us-east-1.rds.amazonaws.com",
        "port": 3306,
        "dbname": "ecommerce",
        "dbInstanceIdentifier": "ecommerce-mysql-db"
    }' \
    --region us-east-1
```

2. Create JWT secret:
```bash
aws secretsmanager create-secret \
    --name "ecommerce/app/jwt-secret" \
    --description "JWT secret key for E-commerce application" \
    --secret-string "your-jwt-secret-key" \
    --region us-east-1
```

## Environment Variables

### Required Environment Variables for ECS

```bash
# Spring Profile
SPRING_PROFILES_ACTIVE=aws

# AWS Configuration
AWS_REGION=us-east-1

# Database Configuration (retrieved from Secrets Manager)
DB_HOST=ecommerce-mysql-db.cwhegcq847i3.us-east-1.rds.amazonaws.com
DB_PORT=3306
DB_NAME=ecommerce
DB_USERNAME=admin

# Secrets Manager
AWS_SECRETS_ENABLED=true
DB_SECRET_NAME=ecommerce/db/credentials
JWT_SECRET_NAME=ecommerce/app/jwt-secret

# S3 Configuration
S3_BUCKET_NAME=ecommerce-invoices-bucket
AWS_S3_REGION=us-east-1

# Application Settings
SERVER_PORT=8080
CONTEXT_PATH=/api/v1
```

### Optional Environment Variables

```bash
# Logging
LOG_LEVEL_APP=INFO
LOG_LEVEL_AWS_SDK=WARN

# CloudWatch Metrics
CLOUDWATCH_METRICS_ENABLED=true
CLOUDWATCH_NAMESPACE=ECommerce/Backend

# Feature Flags
FEATURE_INVENTORY_TRACKING=true
FEATURE_ANALYTICS=true
PAYMENT_MOCK_ENABLED=true

# Performance Tuning
DB_POOL_SIZE=20
DB_MIN_IDLE=5
TASK_CORE_POOL_SIZE=5
TASK_MAX_POOL_SIZE=20
```

## Local Testing with AWS

### Using LocalStack
1. Start LocalStack services:
```bash
cd backend
docker-compose -f docker-compose-aws.yml up -d localstack
```

2. Configure LocalStack endpoints:
```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
export AWS_DEFAULT_REGION=us-east-1
```

3. Create local secrets:
```bash
aws --endpoint-url=http://localhost:4566 secretsmanager create-secret \
    --name "ecommerce/db/credentials" \
    --secret-string '{"username":"admin","password":"password","host":"localhost","port":3306,"dbname":"ecommerce"}'
```

### Using Real AWS Services
1. Configure AWS credentials:
```bash
aws configure
```

2. Set environment variables:
```bash
export SPRING_PROFILES_ACTIVE=aws
export AWS_SECRETS_ENABLED=true
export DB_SECRET_NAME=ecommerce/db/credentials
```

3. Run the application:
```bash
./mvnw spring-boot:run
```

## ECS Deployment

### 1. Build and Push Docker Image
```bash
# Build the image
docker build -t ecommerce-backend .

# Tag for ECR
docker tag ecommerce-backend:latest 123456789012.dkr.ecr.us-east-1.amazonaws.com/ecommerce-backend:latest

# Push to ECR
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/ecommerce-backend:latest
```

### 2. Create ECS Task Definition
```json
{
  "family": "ecommerce-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::123456789012:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::123456789012:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "ecommerce-backend",
      "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/ecommerce-backend:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "aws"
        },
        {
          "name": "AWS_REGION",
          "value": "us-east-1"
        },
        {
          "name": "AWS_SECRETS_ENABLED",
          "value": "true"
        },
        {
          "name": "DB_SECRET_NAME",
          "value": "ecommerce/db/credentials"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/ecommerce-backend",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

### 3. Create ECS Service
```bash
aws ecs create-service \
    --cluster ecommerce-cluster \
    --service-name ecommerce-backend-service \
    --task-definition ecommerce-backend:1 \
    --desired-count 2 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-12345,subnet-67890],securityGroups=[sg-12345],assignPublicIp=ENABLED}"
```

## IAM Permissions

### ECS Task Execution Role
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Resource": "*"
        }
    ]
}
```

### ECS Task Role
```json
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
                "arn:aws:secretsmanager:us-east-1:*:secret:ecommerce/db/credentials*",
                "arn:aws:secretsmanager:us-east-1:*:secret:ecommerce/app/jwt-secret*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:PutObject",
                "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::ecommerce-invoices-bucket/*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "dynamodb:GetItem",
                "dynamodb:PutItem",
                "dynamodb:UpdateItem",
                "dynamodb:DeleteItem",
                "dynamodb:Query",
                "dynamodb:Scan"
            ],
            "Resource": "arn:aws:dynamodb:us-east-1:*:table/ecommerce-*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "sqs:SendMessage",
                "sqs:ReceiveMessage",
                "sqs:DeleteMessage"
            ],
            "Resource": "arn:aws:sqs:us-east-1:*:ecommerce-*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "events:PutEvents"
            ],
            "Resource": "arn:aws:events:us-east-1:*:event-bus/ecommerce-events"
        },
        {
            "Effect": "Allow",
            "Action": [
                "cloudwatch:PutMetricData"
            ],
            "Resource": "*"
        }
    ]
}
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
- **Problem**: Cannot connect to RDS database
- **Solution**: 
  - Check security groups allow inbound traffic on port 3306
  - Verify VPC configuration and subnets
  - Ensure RDS instance is in the same VPC as ECS tasks

#### 2. Secrets Manager Access Denied
- **Problem**: Cannot retrieve secrets from Secrets Manager
- **Solution**:
  - Verify ECS task role has `secretsmanager:GetSecretValue` permission
  - Check secret name and region are correct
  - Ensure secret exists in the specified region

#### 3. Application Startup Issues
- **Problem**: Application fails to start with AWS profile
- **Solution**:
  - Check CloudWatch logs for detailed error messages
  - Verify all required environment variables are set
  - Ensure AWS SDK can authenticate (check IAM roles)

#### 4. Performance Issues
- **Problem**: Slow database connections or timeouts
- **Solution**:
  - Adjust connection pool settings in `application-aws.yml`
  - Monitor CloudWatch metrics for database performance
  - Consider using RDS Proxy for connection pooling

### Debugging Commands

```bash
# Check ECS service status
aws ecs describe-services --cluster ecommerce-cluster --services ecommerce-backend-service

# View CloudWatch logs
aws logs describe-log-streams --log-group-name /ecs/ecommerce-backend

# Test secret retrieval
aws secretsmanager get-secret-value --secret-id ecommerce/db/credentials

# Check task health
aws ecs describe-tasks --cluster ecommerce-cluster --tasks <task-arn>
```

### Health Check Endpoints

The application provides several health check endpoints:

- **Basic Health**: `GET /api/v1/actuator/health`
- **Database Health**: `GET /api/v1/actuator/health/db`
- **Application Info**: `GET /api/v1/actuator/info`
- **Metrics**: `GET /api/v1/actuator/metrics`

### Monitoring and Alerts

Set up CloudWatch alarms for:
- Application health check failures
- High error rates
- Database connection issues
- Memory and CPU utilization
- Response time degradation

## Support

For additional support:
1. Check CloudWatch logs for detailed error messages
2. Review AWS service quotas and limits
3. Consult AWS documentation for specific services
4. Use AWS Support if you have a support plan