#!/bin/bash

CLUSTER_NAME="ecommerce-cluster"
SERVICE_NAME="ecommerce-backend-service"
TASK_FAMILY="ecommerce-backend-task"

echo "Updating ECS service with new image..."

# Force new deployment to pull latest image
aws ecs update-service \
  --cluster $CLUSTER_NAME \
  --service $SERVICE_NAME \
  --force-new-deployment

echo "Waiting for service to stabilize..."

# Wait for service to become stable
aws ecs wait services-stable \
  --cluster $CLUSTER_NAME \
  --services $SERVICE_NAME

echo "Service updated successfully!"

# Check service status
aws ecs describe-services \
  --cluster $CLUSTER_NAME \
  --services $SERVICE_NAME \
  --query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount}'