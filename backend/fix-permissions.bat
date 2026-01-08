@echo off
echo Attaching DynamoDB policy to ECS task role...
aws iam attach-role-policy --role-name ecsTaskRole --policy-arn arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess --region us-east-1
echo Policy attached successfully!