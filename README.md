# AWS Order Processing System - Full Stack Implementation

A comprehensive order processing system built with AWS services, demonstrating modern cloud-native architecture patterns.

## 🏗️ System Architecture

### High-Level Components
- **Frontend**: Angular SPA with JWT authentication
- **API Gateway**: Request routing and Lambda authorization
- **Authentication**: JWT-based with custom Lambda authorizer
- **Backend APIs**: Spring Boot on ECS Fargate
- **Order Processing**: Node.js Lambda functions
- **Data Storage**: RDS (PostgreSQL), DynamoDB, S3
- **Messaging**: SQS for async processing
- **Events**: EventBridge for shipping workflows
- **Load Balancing**: Application Load Balancer

### Data Flow
1. User authenticates via Angular → Spring Boot → RDS
2. Product browsing via Angular → ALB → ECS APIs → RDS
3. Order placement via Angular → API Gateway → Lambda → DynamoDB
4. Async processing via SQS → ECS → RDS/DynamoDB updates
5. Shipping events via EventBridge → Country-specific Lambdas

## 📁 Project Structure

```
aws_dev_ordr_systm_lambda_node/
├── frontend/                    # Angular application
├── backend/                     # Spring Boot APIs (ECS)
├── lambda/                      # Node.js Lambda functions
├── infrastructure/              # AWS CDK/CloudFormation
├── docker/                      # Docker configurations
├── docs/                        # Architecture diagrams
└── scripts/                     # Deployment scripts
```

## 🚀 Quick Start

### Prerequisites
- Node.js 18+
- Java 17+
- Docker
- AWS CLI configured
- Angular CLI

### Local Development
```bash
# Frontend
cd frontend && npm install && ng serve

# Backend
cd backend && ./mvnw spring-boot:run

# Lambda functions
cd lambda && npm install && npm run dev
```

## 🔐 Security Features
- JWT authentication with RS256
- API Gateway Lambda authorizers
- IAM roles and policies
- VPC security groups
- Encrypted data at rest and in transit

## 📊 AWS Services Used

| Service | Purpose | Configuration |
|---------|---------|---------------|
| API Gateway | API routing & auth | REST API with Lambda authorizer |
| Lambda | Order processing | Node.js runtime |
| ECS Fargate | Backend APIs | Spring Boot containers |
| RDS | User/Product data | PostgreSQL |
| DynamoDB | Order lifecycle | Single table design |
| S3 | Document storage | Invoice PDFs |
| SQS | Async messaging | Standard queues + DLQ |
| EventBridge | Event routing | Custom event bus |
| ALB | Load balancing | Target groups for ECS |
| ECR | Container registry | Docker images |
| CloudWatch | Monitoring | Logs, metrics, alarms |

## 🎯 Learning Outcomes

- **Microservices Architecture**: Service decomposition and communication
- **Event-Driven Design**: Async processing with SQS and EventBridge
- **Security Best Practices**: JWT, IAM, and API protection
- **Data Strategy**: Choosing between RDS, DynamoDB, and S3
- **Containerization**: Docker and ECS deployment
- **Serverless Computing**: Lambda function design and optimization
- **Monitoring & Observability**: CloudWatch integration

## 📈 Performance & Cost Optimization

- **Lambda**: Cold start optimization and memory tuning
- **ECS**: Auto-scaling and resource allocation
- **RDS**: Connection pooling and read replicas
- **DynamoDB**: Partition key design and GSI usage
- **S3**: Storage classes and lifecycle policies

## 🔄 CI/CD Pipeline

- GitHub Actions for automated testing
- AWS CodePipeline for deployment
- Blue-green deployment strategy
- Infrastructure as Code with CDK

## 📚 Course Sections Mapping

1. **Architecture & Planning** → System design decisions
2. **Frontend Development** → Angular SPA with routing
3. **Authentication** → JWT + Lambda authorizers
4. **Data Management** → RDS setup and schema design
5. **Backend APIs** → Spring Boot on ECS
6. **File Storage** → S3 integration
7. **Order Processing** → Lambda functions
8. **Order Lifecycle** → DynamoDB operations
9. **Async Processing** → SQS implementation
10. **Order Processing Service** → ECS consumer
11. **Containerization** → Docker + ECR
12. **Load Balancing** → ALB configuration
13. **Event Processing** → EventBridge workflows
14. **Order Tracking** → Real-time status updates
15. **Production Readiness** → Monitoring and optimization
16. **Review & Next Steps** → Architecture patterns

## 🛠️ Development Workflow

1. **Local Development**: Run services locally with Docker Compose
2. **Testing**: Unit tests, integration tests, and E2E tests
3. **Staging**: Deploy to AWS staging environment
4. **Production**: Blue-green deployment with rollback capability

## 📋 API Endpoints

### Authentication
- `POST /auth/register` - User registration
- `POST /auth/login` - User login
- `POST /auth/refresh` - Token refresh

### Products
- `GET /products` - List products
- `GET /products/{id}` - Product details

### Cart
- `GET /cart` - View cart
- `POST /cart/items` - Add to cart
- `DELETE /cart/items/{id}` - Remove from cart

### Orders
- `POST /orders` - Place order
- `GET /orders` - Order history
- `GET /orders/{id}` - Order details
- `GET /orders/{id}/status` - Order status

## 🎨 Frontend Features

- Responsive design with Angular Material
- JWT token management
- Real-time order status updates
- Shopping cart functionality
- Order history and tracking

## 🔧 Configuration

### Environment Variables
```bash
# Backend
DATABASE_URL=jdbc:postgresql://localhost:5432/orders
JWT_SECRET_KEY=your-secret-key
AWS_REGION=us-east-1

# Frontend
API_BASE_URL=https://api.example.com
AWS_REGION=us-east-1
```

## 📖 Documentation

- [Architecture Diagrams](./docs/architecture/)
- [API Documentation](./docs/api/)
- [Deployment Guide](./docs/deployment/)
- [Troubleshooting](./docs/troubleshooting/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ for learning AWS cloud-native development**
