# System Architecture Documentation

## Overview

The AWS Order Processing System is a cloud-native, event-driven microservices architecture designed for scalability, reliability, and cost-effectiveness.

## Architecture Principles

### 1. Microservices Design
- **Service Decomposition**: Each service has a single responsibility
- **Data Isolation**: Each service owns its data
- **Independent Deployment**: Services can be deployed independently
- **Technology Diversity**: Right tool for the right job

### 2. Event-Driven Architecture
- **Asynchronous Processing**: Non-blocking operations
- **Loose Coupling**: Services communicate via events
- **Scalability**: Handle varying loads efficiently
- **Resilience**: Fault tolerance through event replay

### 3. Cloud-Native Patterns
- **Serverless First**: Use Lambda where appropriate
- **Managed Services**: Leverage AWS managed services
- **Auto-Scaling**: Automatic capacity management
- **Pay-per-Use**: Cost optimization

## Component Architecture

### Frontend Layer
```
┌─────────────────┐
│   Angular SPA   │
│                 │
│ - JWT Auth      │
│ - Material UI   │
│ - State Mgmt    │
│ - HTTP Client   │
└─────────────────┘
```

### API Gateway Layer
```
┌─────────────────┐
│  API Gateway    │
│                 │
│ - Rate Limiting │
│ - CORS          │
│ - Lambda Auth   │
│ - Request/Resp  │
└─────────────────┘
```

### Authentication Service
```
┌─────────────────┐
│ Lambda Authorizer│
│                 │
│ - JWT Validation│
│ - JWKS Client   │
│ - User Context  │
│ - IAM Policies  │
└─────────────────┘
```

### Backend Services
```
┌─────────────────┐    ┌─────────────────┐
│  Product API    │    │   Cart API      │
│  (ECS Fargate)  │    │  (ECS Fargate)  │
│                 │    │                 │
│ - CRUD Ops      │    │ - Session Mgmt  │
│ - Caching       │    │ - Redis Cache   │
│ - Validation    │    │ - Price Calc    │
└─────────────────┘    └─────────────────┘
```

### Order Processing
```
┌─────────────────┐
│ Order Lambda    │
│                 │
│ - Order Create  │
│ - Invoice Gen   │
│ - Event Publish │
│ - S3 Upload     │
└─────────────────┘
```

### Data Layer
```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│     RDS     │  │  DynamoDB   │  │     S3      │
│ PostgreSQL  │  │             │  │             │
│             │  │ - Orders    │  │ - Invoices  │
│ - Users     │  │ - Status    │  │ - Documents │
│ - Products  │  │ - Tracking  │  │ - Backups   │
│ - Cart      │  │             │  │             │
└─────────────┘  └─────────────┘  └─────────────┘
```

## Data Flow Diagrams

### User Authentication Flow
```
User → Angular → API Gateway → Lambda Authorizer → JWT Validation → User Context
```

### Product Browsing Flow
```
User → Angular → ALB → ECS (Product API) → RDS → Response
```

### Order Placement Flow
```
User → Angular → API Gateway → Order Lambda → DynamoDB + S3 → SQS → ECS Processing
```

### Event Processing Flow
```
Order Created → EventBridge → Country Router → Shipping Lambda → Status Update
```

## Security Architecture

### Authentication & Authorization
- **JWT Tokens**: RS256 signed tokens
- **Lambda Authorizer**: Custom authorization logic
- **IAM Roles**: Least privilege access
- **API Gateway**: Request validation

### Data Protection
- **Encryption at Rest**: RDS, DynamoDB, S3
- **Encryption in Transit**: TLS 1.2+
- **VPC**: Network isolation
- **Security Groups**: Firewall rules

### Secrets Management
- **AWS Secrets Manager**: Database credentials
- **Parameter Store**: Configuration values
- **Environment Variables**: Runtime config

## Scalability Patterns

### Horizontal Scaling
- **ECS Auto Scaling**: CPU/Memory based
- **Lambda Concurrency**: Automatic scaling
- **RDS Read Replicas**: Read scaling
- **DynamoDB On-Demand**: Automatic capacity

### Caching Strategy
- **Redis**: Session and cart caching
- **CloudFront**: Static content CDN
- **Application Cache**: In-memory caching

### Load Distribution
- **Application Load Balancer**: Traffic distribution
- **API Gateway**: Request throttling
- **SQS**: Message buffering
- **EventBridge**: Event routing

## Monitoring & Observability

### Logging
- **CloudWatch Logs**: Centralized logging
- **Structured Logging**: JSON format
- **Log Aggregation**: Cross-service correlation
- **Log Retention**: Cost-optimized retention

### Metrics
- **CloudWatch Metrics**: System metrics
- **Custom Metrics**: Business metrics
- **Dashboards**: Real-time monitoring
- **Alarms**: Proactive alerting

### Tracing
- **X-Ray**: Distributed tracing
- **Request ID**: End-to-end tracking
- **Performance Monitoring**: Latency analysis

## Disaster Recovery

### Backup Strategy
- **RDS Automated Backups**: Point-in-time recovery
- **DynamoDB Backups**: Continuous backups
- **S3 Versioning**: Object versioning
- **Cross-Region Replication**: Geographic redundancy

### High Availability
- **Multi-AZ Deployment**: RDS failover
- **ECS Service**: Health checks and replacement
- **Lambda**: Built-in redundancy
- **Load Balancer**: Health monitoring

## Cost Optimization

### Resource Optimization
- **Right-Sizing**: Appropriate instance types
- **Reserved Instances**: Long-term commitments
- **Spot Instances**: Cost-effective compute
- **Serverless**: Pay-per-use model

### Storage Optimization
- **S3 Storage Classes**: Lifecycle policies
- **DynamoDB On-Demand**: Usage-based pricing
- **RDS Storage**: GP2 to GP3 migration

## Performance Optimization

### Database Performance
- **Connection Pooling**: Efficient connections
- **Query Optimization**: Index usage
- **Read Replicas**: Read scaling
- **Caching**: Reduced database load

### Application Performance
- **Async Processing**: Non-blocking operations
- **Batch Processing**: Efficient bulk operations
- **CDN**: Global content delivery
- **Compression**: Reduced payload size

## Technology Stack Summary

| Layer | Technology | Purpose |
|-------|------------|---------|
| Frontend | Angular 16 | User interface |
| API Gateway | AWS API Gateway | Request routing |
| Authentication | Lambda + JWT | User authentication |
| Backend APIs | Spring Boot 3 | Business logic |
| Container Platform | ECS Fargate | Container orchestration |
| Serverless | Lambda (Node.js) | Event processing |
| Relational DB | RDS PostgreSQL | Structured data |
| NoSQL DB | DynamoDB | Order lifecycle |
| Object Storage | S3 | Document storage |
| Message Queue | SQS | Async messaging |
| Event Bus | EventBridge | Event routing |
| Load Balancer | ALB | Traffic distribution |
| Container Registry | ECR | Docker images |
| Monitoring | CloudWatch | Observability |
| CDN | CloudFront | Content delivery |