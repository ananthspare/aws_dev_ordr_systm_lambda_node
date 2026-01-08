#!/bin/bash

# AWS Order Processing System Deployment Script
# Usage: ./deploy.sh [dev|prod]

set -e

ENVIRONMENT=${1:-dev}
AWS_REGION=${AWS_REGION:-us-east-1}

echo "🚀 Deploying Order Processing System to $ENVIRONMENT environment"

# Check prerequisites
check_prerequisites() {
    echo "📋 Checking prerequisites..."
    
    if ! command -v aws &> /dev/null; then
        echo "❌ AWS CLI not found. Please install AWS CLI."
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        echo "❌ Docker not found. Please install Docker."
        exit 1
    fi
    
    if ! command -v node &> /dev/null; then
        echo "❌ Node.js not found. Please install Node.js."
        exit 1
    fi
    
    if ! command -v java &> /dev/null; then
        echo "❌ Java not found. Please install Java 17+."
        exit 1
    fi
    
    echo "✅ Prerequisites check passed"
}

# Build all components
build_components() {
    echo "🔨 Building all components..."
    
    # Build frontend
    echo "📦 Building Angular frontend..."
    cd frontend
    npm install
    npm run build:prod
    cd ..
    
    # Build backend
    echo "📦 Building Spring Boot backend..."
    cd backend
    ./mvnw clean package -DskipTests
    cd ..
    
    # Build Lambda functions
    echo "📦 Building Lambda functions..."
    cd lambda
    npm install
    npm run build
    cd ..
    
    echo "✅ All components built successfully"
}

# Deploy infrastructure
deploy_infrastructure() {
    echo "🏗️ Deploying infrastructure..."
    cd infrastructure
    npm install
    npm run build
    cdk deploy --require-approval never --profile $ENVIRONMENT
    cd ..
    echo "✅ Infrastructure deployed"
}

# Deploy Lambda functions
deploy_lambda() {
    echo "⚡ Deploying Lambda functions..."
    cd lambda
    npm run deploy:$ENVIRONMENT
    cd ..
    echo "✅ Lambda functions deployed"
}

# Build and push Docker images
deploy_containers() {
    echo "🐳 Building and pushing Docker images..."
    
    # Get ECR repository URI
    ECR_URI=$(aws ecr describe-repositories --repository-names order-processing-backend --region $AWS_REGION --query 'repositories[0].repositoryUri' --output text)
    
    # Login to ECR
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URI
    
    # Build and push backend image
    cd backend
    docker build -t order-processing-backend .
    docker tag order-processing-backend:latest $ECR_URI:latest
    docker push $ECR_URI:latest
    cd ..
    
    echo "✅ Docker images deployed"
}

# Update ECS service
update_ecs() {
    echo "🚢 Updating ECS service..."
    aws ecs update-service --cluster order-processing-cluster --service backend-service --force-new-deployment --region $AWS_REGION
    echo "✅ ECS service updated"
}

# Deploy frontend to S3
deploy_frontend() {
    echo "🌐 Deploying frontend to S3..."
    
    S3_BUCKET="order-processing-frontend-$ENVIRONMENT"
    
    # Sync frontend build to S3
    aws s3 sync frontend/dist/ s3://$S3_BUCKET --delete
    
    # Invalidate CloudFront cache
    DISTRIBUTION_ID=$(aws cloudfront list-distributions --query "DistributionList.Items[?Origins.Items[0].DomainName=='$S3_BUCKET.s3.amazonaws.com'].Id" --output text)
    if [ ! -z "$DISTRIBUTION_ID" ]; then
        aws cloudfront create-invalidation --distribution-id $DISTRIBUTION_ID --paths "/*"
    fi
    
    echo "✅ Frontend deployed"
}

# Main deployment flow
main() {
    echo "🎯 Starting deployment for environment: $ENVIRONMENT"
    
    check_prerequisites
    build_components
    deploy_infrastructure
    deploy_lambda
    deploy_containers
    update_ecs
    deploy_frontend
    
    echo "🎉 Deployment completed successfully!"
    echo "📊 Check AWS Console for service status"
}

# Run main function
main