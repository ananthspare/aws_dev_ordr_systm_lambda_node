@echo off
echo Building Maven project...
call mvn clean package -DskipTests
if %errorlevel% neq 0 exit /b %errorlevel%

echo Authenticating with ECR...
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 411993131705.dkr.ecr.us-east-1.amazonaws.com
if %errorlevel% neq 0 exit /b %errorlevel%

echo Building Docker image...
docker build -t ecommerce-backend .
if %errorlevel% neq 0 exit /b %errorlevel%

echo Tagging image...
docker tag ecommerce-backend:latest 411993131705.dkr.ecr.us-east-1.amazonaws.com/ecommerce-backend:latest
if %errorlevel% neq 0 exit /b %errorlevel%

echo Pushing to ECR...
docker push 411993131705.dkr.ecr.us-east-1.amazonaws.com/ecommerce-backend:latest
if %errorlevel% neq 0 exit /b %errorlevel%

echo Restarting ECS service...
aws ecs update-service --cluster ecommerce-cluster --service ecommerce-backend-service --force-new-deployment --region us-east-1
echo Deploy completed successfully!