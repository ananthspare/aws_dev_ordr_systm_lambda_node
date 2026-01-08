# E-commerce Backend API

A comprehensive Spring Boot REST API for an e-commerce system with MySQL database integration and AWS Secrets Manager support.

## Features

- **Customer Management**: Registration, authentication, profile management
- **Product Catalog**: Product CRUD operations with inventory management
- **Order Processing**: Order creation, status tracking, and management
- **Payment Processing**: Payment handling with multiple payment methods
- **Security**: JWT authentication, password encryption, CORS support
- **Database**: MySQL with JPA/Hibernate ORM
- **AWS Integration**: Secrets Manager for secure credential management
- **API Documentation**: Swagger/OpenAPI 3.0
- **Monitoring**: Spring Boot Actuator endpoints

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **Spring Data JPA**
- **Spring Security**
- **MySQL 8.0**
- **AWS SDK v2**
- **JWT (JSON Web Tokens)**
- **Swagger/OpenAPI 3.0**
- **Lombok**
- **MapStruct**

## Database Schema

The application uses the following MySQL tables:

### Customers
```sql
CREATE TABLE customers (
  customer_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name          VARCHAR(150) NOT NULL,
  email         VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (customer_id),
  UNIQUE KEY uq_customers_email (email)
) ENGINE=InnoDB;
```

### Products
```sql
CREATE TABLE products (
  product_id   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name         VARCHAR(255) NOT NULL,
  description  VARCHAR(500) NULL,
  price        DECIMAL(10,2) NOT NULL,
  stock_qty    INT NOT NULL DEFAULT 0,
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (product_id)
) ENGINE=InnoDB;
```

### Orders
```sql
CREATE TABLE orders (
  order_id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  customer_id       BIGINT UNSIGNED NOT NULL,
  status            VARCHAR(30) NOT NULL DEFAULT 'CREATED',
  country_code      CHAR(2) NOT NULL,
  total_amount      DECIMAL(10,2) NOT NULL,
  invoice_s3_path   VARCHAR(500) NULL,
  payment_status    VARCHAR(20),
  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (order_id),
  KEY idx_orders_customer (customer_id),
  KEY idx_orders_status (status),
  CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
) ENGINE=InnoDB;
```

### Order Items
```sql
CREATE TABLE order_items (
  order_item_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id      BIGINT UNSIGNED NOT NULL,
  product_id    BIGINT UNSIGNED NOT NULL,
  quantity      INT NOT NULL,
  unit_price    DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (order_item_id),
  KEY idx_items_order (order_id),
  CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id),
  CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(product_id)
) ENGINE=InnoDB;
```

### Payments
```sql
CREATE TABLE payments (
  payment_id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id        BIGINT UNSIGNED NOT NULL,
  payment_method  VARCHAR(30) NOT NULL DEFAULT 'MOCK',
  status          VARCHAR(20) NOT NULL DEFAULT 'PAID',
  amount          DECIMAL(10,2) NOT NULL,
  currency        CHAR(3) NOT NULL DEFAULT 'USD',
  transaction_ref VARCHAR(100) NULL,
  created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (payment_id),
  UNIQUE KEY uq_payments_order (order_id),
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(order_id)
) ENGINE=InnoDB;
```

## Configuration

### Database Connection

The application connects to your MySQL RDS instance:

```yaml
spring:
  datasource:
    url: jdbc:mysql://ecommerce-mysql-db.cwhegcq847i3.us-east-1.rds.amazonaws.com:3306/ecommerce
    username: admin
    password: ${DB_PASSWORD}
```

### Environment Variables

Set the following environment variables:

```bash
# Database
DB_PASSWORD=your_database_password

# JWT
JWT_SECRET=your_jwt_secret_key

# AWS
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# Optional: AWS Secrets Manager
DB_SECRET_NAME=ecommerce-db-credentials
```

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new customer |
| POST | `/api/v1/auth/login` | Customer login |
| POST | `/api/v1/auth/logout` | Customer logout |
| POST | `/api/v1/auth/refresh` | Refresh authentication token |
| GET | `/api/v1/auth/check-email` | Check email availability |

### Customer Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/customers` | Create new customer |
| GET | `/api/v1/customers/{id}` | Get customer by ID |
| GET | `/api/v1/customers/email/{email}` | Get customer by email |
| GET | `/api/v1/customers` | Get all customers (paginated) |
| GET | `/api/v1/customers/search` | Search customers |
| GET | `/api/v1/customers/with-orders` | Get customers with orders |
| GET | `/api/v1/customers/without-orders` | Get customers without orders |
| GET | `/api/v1/customers/created-after` | Get customers created after date |
| GET | `/api/v1/customers/count` | Get total customer count |
| GET | `/api/v1/customers/exists/{email}` | Check if email exists |
| PUT | `/api/v1/customers/{id}` | Update customer |
| PUT | `/api/v1/customers/{id}/password` | Change password |
| DELETE | `/api/v1/customers/{id}` | Delete customer |

### Product Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/products` | Create new product |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products` | Get all products (paginated) |
| GET | `/api/v1/products/search` | Search products |
| GET | `/api/v1/products/price-range` | Get products by price range |
| GET | `/api/v1/products/in-stock` | Get in-stock products |
| GET | `/api/v1/products/out-of-stock` | Get out-of-stock products |
| GET | `/api/v1/products/low-stock` | Get low stock products |
| GET | `/api/v1/products/top-selling` | Get top selling products |
| GET | `/api/v1/products/available/price-range` | Get available products by price |
| GET | `/api/v1/products/created-after` | Get products created after date |
| GET | `/api/v1/products/statistics/stock` | Get stock statistics |
| GET | `/api/v1/products/{id}/stock/check` | Check product stock |
| PUT | `/api/v1/products/{id}` | Update product |
| PUT | `/api/v1/products/{id}/stock` | Update stock |
| DELETE | `/api/v1/products/{id}` | Delete product |

### Order Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Create new order |
| GET | `/api/v1/orders/{id}` | Get order by ID |
| GET | `/api/v1/orders` | Get all orders (paginated) |
| GET | `/api/v1/orders/customer/{customerId}` | Get orders by customer |
| GET | `/api/v1/orders/status/{status}` | Get orders by status |
| GET | `/api/v1/orders/payment-status/{status}` | Get orders by payment status |
| GET | `/api/v1/orders/country/{countryCode}` | Get orders by country |
| GET | `/api/v1/orders/date-range` | Get orders by date range |
| GET | `/api/v1/orders/search` | Search orders by customer |
| GET | `/api/v1/orders/with-invoice` | Get orders with invoices |
| GET | `/api/v1/orders/without-invoice` | Get orders without invoices |
| GET | `/api/v1/orders/customer/{id}/recent` | Get recent orders for customer |
| GET | `/api/v1/orders/revenue/total` | Get total revenue |
| GET | `/api/v1/orders/revenue/date-range` | Get revenue by date range |
| GET | `/api/v1/orders/revenue/country/{code}` | Get revenue by country |
| GET | `/api/v1/orders/statistics` | Get order statistics |
| PUT | `/api/v1/orders/{id}/status` | Update order status |
| PUT | `/api/v1/orders/{id}/payment-status` | Update payment status |
| PUT | `/api/v1/orders/{id}/invoice` | Update invoice S3 path |
| PUT | `/api/v1/orders/{id}/cancel` | Cancel order |

### Payment Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Create payment |
| POST | `/api/v1/payments/{id}/process` | Process payment |
| POST | `/api/v1/payments/{id}/refund` | Refund payment |
| GET | `/api/v1/payments/{id}` | Get payment by ID |
| GET | `/api/v1/payments/order/{orderId}` | Get payment by order |
| GET | `/api/v1/payments/transaction/{ref}` | Get payment by transaction ref |
| GET | `/api/v1/payments` | Get all payments (paginated) |
| GET | `/api/v1/payments/status/{status}` | Get payments by status |
| GET | `/api/v1/payments/method/{method}` | Get payments by method |
| GET | `/api/v1/payments/customer/{customerId}` | Get payments by customer |
| GET | `/api/v1/payments/date-range` | Get payments by date range |
| GET | `/api/v1/payments/amount-range` | Get payments by amount range |
| GET | `/api/v1/payments/currency/{currency}` | Get payments by currency |
| GET | `/api/v1/payments/successful` | Get successful payments |
| GET | `/api/v1/payments/failed` | Get failed payments |
| GET | `/api/v1/payments/pending` | Get pending payments |
| GET | `/api/v1/payments/retry` | Get payments requiring retry |
| GET | `/api/v1/payments/customer/{id}/recent` | Get recent payments for customer |
| GET | `/api/v1/payments/statistics` | Get payment statistics |
| GET | `/api/v1/payments/total/status/{status}` | Get total amount by status |
| GET | `/api/v1/payments/total/method/{method}` | Get total amount by method |
| GET | `/api/v1/payments/success-rate` | Get payment success rate |
| PUT | `/api/v1/payments/{id}/status` | Update payment status |

### Order Item Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/order-items/{id}` | Get order item by ID |
| GET | `/api/v1/order-items` | Get all order items (paginated) |
| GET | `/api/v1/order-items/order/{orderId}` | Get order items by order |
| GET | `/api/v1/order-items/order/{orderId}/paginated` | Get order items by order (paginated) |
| GET | `/api/v1/order-items/product/{productId}` | Get order items by product |
| GET | `/api/v1/order-items/customer/{customerId}` | Get order items by customer |
| GET | `/api/v1/order-items/search/product` | Search order items by product name |
| GET | `/api/v1/order-items/order-status/{status}` | Get order items by order status |
| GET | `/api/v1/order-items/quantity-greater-than/{qty}` | Get order items by quantity |
| GET | `/api/v1/order-items/price-range` | Get order items by price range |
| GET | `/api/v1/order-items/product/{id}/total-quantity` | Get total quantity sold for product |
| GET | `/api/v1/order-items/product/{id}/total-revenue` | Get total revenue for product |
| GET | `/api/v1/order-items/analytics/top-selling-by-quantity` | Get top selling products by quantity |
| GET | `/api/v1/order-items/analytics/top-selling-by-revenue` | Get top selling products by revenue |
| GET | `/api/v1/order-items/order/{id}/total` | Calculate order total |
| GET | `/api/v1/order-items/product/{id}/statistics` | Get product sales statistics |
| GET | `/api/v1/order-items/order/{id}/count` | Count order items by order |
| PUT | `/api/v1/order-items/{id}` | Update order item |
| DELETE | `/api/v1/order-items/{id}` | Delete order item |

## Running the Application

### Prerequisites

1. **Java 17** or higher
2. **Maven 3.6+**
3. **MySQL 8.0** database
4. **AWS Account** (for Secrets Manager integration)

### Local Development

1. **Clone the repository**
```bash
git clone <repository-url>
cd backend
```

2. **Set environment variables**
```bash
export DB_PASSWORD=your_database_password
export JWT_SECRET=your_jwt_secret_key
export AWS_REGION=us-east-1
```

3. **Run the application**
```bash
./mvnw spring-boot:run
```

4. **Access the API**
- API Base URL: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Check: `http://localhost:8080/actuator/health`

### Docker Deployment

1. **Build Docker image**
```bash
docker build -t ecommerce-backend .
```

2. **Run container**
```bash
docker run -p 8080:8080 \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  -e AWS_REGION=us-east-1 \
  ecommerce-backend
```

## Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Tests
```bash
./mvnw test -Dtest=**/*IntegrationTest
```

## API Documentation

The API is documented using OpenAPI 3.0 specification. Access the interactive documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Security

### Authentication
- JWT-based authentication
- Password encryption using BCrypt
- Secure password policies

### Authorization
- Role-based access control (future enhancement)
- API endpoint protection

### Data Protection
- Input validation and sanitization
- SQL injection prevention
- XSS protection

## Monitoring

### Health Checks
- Application health: `/actuator/health`
- Database connectivity check
- Custom health indicators

### Metrics
- JVM metrics
- Database connection pool metrics
- Custom business metrics

## Error Handling

The API provides consistent error responses:

```json
{
  "timestamp": "2024-01-02T10:30:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "Customer not found with ID: 123",
  "path": "/api/v1/customers/123"
}
```

## Performance Considerations

### Database Optimization
- Connection pooling with HikariCP
- Query optimization with JPA
- Database indexing strategy

### Caching
- Application-level caching (future enhancement)
- Database query result caching

### Pagination
- Consistent pagination across all list endpoints
- Configurable page sizes with limits

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.