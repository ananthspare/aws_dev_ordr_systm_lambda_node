# AWS Secrets Manager Setup Script for E-commerce Backend (PowerShell)
# This script creates the necessary secrets in AWS Secrets Manager

param(
    [string]$Region = "us-east-1",
    [string]$SecretName = "ecommerce/db/credentials",
    [string]$DbHost = "ecommerce-mysql-db.cwhegcq847i3.us-east-1.rds.amazonaws.com",
    [int]$DbPort = 3306,
    [string]$DbName = "ecommerce",
    [string]$DbUsername = "admin"
)

# Colors for output
$Green = "Green"
$Yellow = "Yellow"
$Red = "Red"

Write-Host "Setting up AWS Secrets Manager for E-commerce Backend" -ForegroundColor $Green
Write-Host "======================================================" -ForegroundColor $Green

# Check if AWS CLI is installed
try {
    $null = Get-Command aws -ErrorAction Stop
} catch {
    Write-Host "Error: AWS CLI is not installed. Please install it first." -ForegroundColor $Red
    exit 1
}

# Check if AWS credentials are configured
try {
    $identity = aws sts get-caller-identity --output json | ConvertFrom-Json
    Write-Host "Current AWS Identity:" -ForegroundColor $Yellow
    Write-Host "Account: $($identity.Account)" -ForegroundColor $Yellow
    Write-Host "User: $($identity.Arn)" -ForegroundColor $Yellow
} catch {
    Write-Host "Error: AWS credentials are not configured. Please run 'aws configure' first." -ForegroundColor $Red
    exit 1
}

# Prompt for database password
$DbPassword = Read-Host "Enter the database password for user '$DbUsername'" -AsSecureString
$DbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($DbPassword))

if ([string]::IsNullOrEmpty($DbPasswordPlain)) {
    Write-Host "Error: Database password cannot be empty." -ForegroundColor $Red
    exit 1
}

# Create the secret JSON
$SecretValue = @{
    username = $DbUsername
    password = $DbPasswordPlain
    engine = "mysql"
    host = $DbHost
    port = $DbPort
    dbname = $DbName
    dbInstanceIdentifier = "ecommerce-mysql-db"
} | ConvertTo-Json -Compress

Write-Host "Creating secret in AWS Secrets Manager..." -ForegroundColor $Yellow

# Check if secret already exists
try {
    $null = aws secretsmanager describe-secret --secret-id $SecretName --region $Region 2>$null
    Write-Host "Secret already exists. Updating..." -ForegroundColor $Yellow
    aws secretsmanager update-secret --secret-id $SecretName --secret-string $SecretValue --region $Region
    Write-Host "Secret updated successfully!" -ForegroundColor $Green
} catch {
    Write-Host "Creating new secret..." -ForegroundColor $Yellow
    aws secretsmanager create-secret --name $SecretName --description "Database credentials for E-commerce application" --secret-string $SecretValue --region $Region
    Write-Host "Secret created successfully!" -ForegroundColor $Green
}

# Create JWT secret
$JwtSecretName = "ecommerce/app/jwt-secret"
$JwtSecretValue = [System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes([System.Guid]::NewGuid().ToString() + [System.Guid]::NewGuid().ToString()))

Write-Host "Creating JWT secret..." -ForegroundColor $Yellow
try {
    $null = aws secretsmanager describe-secret --secret-id $JwtSecretName --region $Region 2>$null
    Write-Host "JWT secret already exists. Updating..." -ForegroundColor $Yellow
    aws secretsmanager update-secret --secret-id $JwtSecretName --secret-string $JwtSecretValue --region $Region
} catch {
    aws secretsmanager create-secret --name $JwtSecretName --description "JWT secret key for E-commerce application" --secret-string $JwtSecretValue --region $Region
}

Write-Host "JWT secret configured successfully!" -ForegroundColor $Green

# Display the secrets
Write-Host ""
Write-Host "Secrets created/updated:" -ForegroundColor $Green
Write-Host "1. Database credentials: $SecretName"
Write-Host "2. JWT secret: $JwtSecretName"

Write-Host ""
Write-Host "To use these secrets in your application:" -ForegroundColor $Yellow
Write-Host "1. Ensure your ECS task role has the following permissions:"
Write-Host "   - secretsmanager:GetSecretValue"
Write-Host "   - secretsmanager:DescribeSecret"
Write-Host ""
Write-Host "2. Set the following environment variables in your ECS task definition:"
Write-Host "   - AWS_SECRETS_ENABLED=true"
Write-Host "   - DB_SECRET_NAME=$SecretName"
Write-Host "   - JWT_SECRET_NAME=$JwtSecretName"
Write-Host ""
Write-Host "Setup completed successfully!" -ForegroundColor $Green

# Create IAM policy document for reference
$IamPolicy = @{
    Version = "2012-10-17"
    Statement = @(
        @{
            Effect = "Allow"
            Action = @(
                "secretsmanager:GetSecretValue",
                "secretsmanager:DescribeSecret"
            )
            Resource = @(
                "arn:aws:secretsmanager:${Region}:*:secret:${SecretName}*",
                "arn:aws:secretsmanager:${Region}:*:secret:${JwtSecretName}*"
            )
        }
    )
} | ConvertTo-Json -Depth 10

$IamPolicy | Out-File -FilePath "iam-secrets-policy.json" -Encoding UTF8

Write-Host "IAM policy document created: iam-secrets-policy.json" -ForegroundColor $Yellow
Write-Host "Attach this policy to your ECS task role."

# Clear sensitive variables
$DbPasswordPlain = $null
$SecretValue = $null