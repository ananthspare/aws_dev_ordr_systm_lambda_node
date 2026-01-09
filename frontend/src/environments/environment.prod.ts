export const environment = {
  production: true,
  // Direct ALB URL (bypass API Gateway for development)
  // apiUrl: 'http://ecommerce-alb-596377866.us-east-1.elb.amazonaws.com/api/v1'
  // API Gateway URL (with Lambda authorizer)
  apiUrl: 'https://7yccx37pn2.execute-api.us-east-1.amazonaws.com/prod/api/v1'
  // Local development
  // apiUrl: 'http://localhost:8080/api/v1'
};