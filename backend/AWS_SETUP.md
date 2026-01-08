# AWS Setup Guide for E-Commerce Backend

This guide explains how to configure AWS credentials for the e-commerce backend to connect to DynamoDB and other AWS services.

## Prerequisites

1. AWS Account with appropriate permissions
2. AWS CLI installed
3. Existing DynamoDB table named "Cart" with the correct schema

## AWS Credentials Configuration

### Option 1: AWS Profile (Recommended for Local Development)

1. Configure AWS CLI with your credentials:
```bash
aws configure
```

2. Or create a named profile:
```bash
aws configure --profile ecommerce
```

3. Set the profile in your environment or application.yml:
```bash
export AWS_PROFILE=ecommerce
```

### Option 2: Environment Variables

Set the following environment variables:
```bash
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key
export AWS_REGION=us-east-1
```

### Option 3: IAM Roles (For ECS Deployment)

When deploying to ECS, attach an IAM role to the task with the following permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
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
            "Resource": [
                "arn:aws:dynamodb:us-east-1:*:table/Cart",
                "arn:aws:dynamodb:us-east-1:*:table/Cart/index/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue"
            ],
            "Resource": [
                "arn:aws:secretsmanager:us-east-1:*:secret:ecommerce-db-credentials*"
            ]
        }
    ]
}
```

## DynamoDB Table Schema

The application expects a DynamoDB table named "Cart" with the following structure:

- **Table Name**: Cart
- **Partition Key**: customerId (String)
- **Sort Key**: itemKey (String)
- **TTL Attribute**: ttl (Number) - Optional

### Sample Item Structure:
```json
{
  "customerId": "123",
  "itemKey": "PRODUCT#101",
  "productId": 101,
  "productName": "Wireless Headphones",
  "quantity": 2,
  "unitPrice": 99.99,
  "totalPrice": 199.98,
  "addedAt": "2024-01-04T10:30:00Z",
  "updatedAt": "2024-01-04T10:30:00Z",
  "ttl": 1706875800
}
```

## Testing the Connection

1. Run the application with the `local` profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

2. Check the logs for DynamoDB connection messages
3. Run the cart repository tests:
```bash
mvn test -Dtest=CartRepositoryTest
```

## Troubleshooting

### Common Issues:

1. **Access Denied**: Ensure your AWS credentials have DynamoDB permissions
2. **Table Not Found**: Verify the "Cart" table exists in the correct region
3. **Region Mismatch**: Ensure AWS_REGION matches your DynamoDB table region
4. **Credentials Not Found**: Check AWS credentials configuration

### Debug Logging:

Enable debug logging in application.yml:
```yaml
logging:
  level:
    software.amazon.awssdk: DEBUG
    com.ecommerce: DEBUG
```

## Environment Variables for Different Environments

### Local Development:
```bash
export AWS_PROFILE=default
export AWS_REGION=us-east-1
```

### ECS Deployment:
```bash
export AWS_REGION=us-east-1
# IAM role attached to ECS task provides credentials
```

## Security Best Practices

1. Never commit AWS credentials to version control
2. Use IAM roles for ECS deployments
3. Follow principle of least privilege for permissions
4. Rotate access keys regularly
5. Use AWS Secrets Manager for sensitive configuration