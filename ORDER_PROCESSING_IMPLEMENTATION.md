# Order Processing Implementation

## Overview
This implementation adds checkout functionality that creates orders with CREATED status, payments with PENDING status, and sends messages to SQS for asynchronous processing.

## Architecture Flow

1. **Frontend**: User clicks "Proceed to Checkout"
2. **Backend**: Creates order (CREATED status) and payment (PENDING status)
3. **SQS**: Sends order processing message to queue
4. **Lambda**: Processes order asynchronously

## Backend Changes

### 1. Dependencies Added
- AWS SQS SDK dependency in `pom.xml`

### 2. New Components
- `SqsConfig.java`: Configuration for SQS client
- `SqsService.java`: Service for sending messages to SQS

### 3. Modified Components
- `OrderService.java`: Added SQS message sending after checkout
- `application.yml`: Added SQS queue URL configuration

## Lambda Function

### Order Processor (`lambda/functions/order-processor/`)
- Consumes SQS messages
- Processes order events
- Sends events to EventBridge for further processing

## Configuration

### Environment Variables
```bash
# Backend
ORDER_PROCESSING_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/order-processing-queue

# AWS Region
AWS_REGION=us-east-1
```

### SQS Queue Setup
Create an SQS FIFO queue named `order-processing-queue.fifo` with:
- Message retention: 14 days
- Visibility timeout: 30 seconds
- Dead letter queue configured

## API Usage

### Checkout Endpoint
```http
POST /api/v1/orders/checkout
Content-Type: application/json

{
  "customerId": 1,
  "countryCode": "US",
  "paymentMethod": "CREDIT_CARD",
  "currency": "USD",
  "orderItems": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

### Response
```json
{
  "order": {
    "orderId": 123,
    "status": "CREATED",
    "paymentStatus": "PENDING",
    "totalAmount": 99.99
  },
  "payment": {
    "paymentId": 456,
    "status": "PENDING",
    "amount": 99.99
  },
  "message": "Order placed, payment is pending"
}
```

## Message Flow

### SQS Message Format
```json
{
  "orderId": 123,
  "customerId": 1,
  "countryCode": "US",
  "timestamp": 1641234567890,
  "eventType": "ORDER_CREATED"
}
```

### EventBridge Event Format
```json
{
  "Source": "ecommerce.order-processor",
  "DetailType": "Order Processed",
  "Detail": {
    "orderId": 123,
    "customerId": 1,
    "countryCode": "US",
    "status": "PROCESSED",
    "timestamp": "2024-01-01T12:00:00.000Z"
  }
}
```

## Deployment

### Backend
```bash
cd backend
./mvnw clean package
docker build -t ecommerce-backend .
```

### Lambda Function
```bash
cd lambda/functions/order-processor
npm install
zip -r order-processor.zip .
# Deploy using AWS CLI or Console
```

## Error Handling

- SQS message failures don't affect checkout success
- Dead letter queue captures failed messages
- Lambda retries failed processing automatically
- Comprehensive logging for troubleshooting

## Future Enhancements

1. **Order Status Updates**: Update order status in DynamoDB
2. **Invoice Generation**: Generate PDF invoices and store in S3
3. **Notifications**: Send email/SMS notifications to customers
4. **Shipping Integration**: Trigger shipping workflows via EventBridge
5. **Payment Processing**: Integrate with real payment gateways