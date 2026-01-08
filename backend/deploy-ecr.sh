#!/bin/bash

# Variables
REGION="us-east-1"
REPOSITORY_NAME="ecommerce-backend"
IMAGE_TAG="latest"

# Get AWS Account ID
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo "AWS Account ID: $ACCOUNT_ID"

# Create ECR repository if it doesn't exist
echo "Creating ECR repository..."
aws ecr create-repository --repository-name $REPOSITORY_NAME --region $REGION 2>/dev/null || echo "Repository already exists"

# Build JAR file
echo "Building application..."
mvn clean package -DskipTests

# Build Docker image
echo "Building Docker image..."
docker build -t $REPOSITORY_NAME:$IMAGE_TAG .

# Tag image for ECR
echo "Tagging image for ECR..."
docker tag $REPOSITORY_NAME:$IMAGE_TAG $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$REPOSITORY_NAME:$IMAGE_TAG

# Login to ECR
echo "Logging in to ECR..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com

# Push image to ECR
echo "Pushing image to ECR..."
docker push $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$REPOSITORY_NAME:$IMAGE_TAG

echo "Image pushed successfully!"
echo "Image URI: $ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$REPOSITORY_NAME:$IMAGE_TAG"