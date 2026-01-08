package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDBConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.profile:default}")
    private String awsProfile;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
                .region(Region.of(awsRegion));

        // Use profile credentials for local development, default for ECS
        try {
            // Try profile credentials first (for local development)
            if (awsProfile != null && !awsProfile.isEmpty() && !awsProfile.equals("default")) {
                builder.credentialsProvider(ProfileCredentialsProvider.create(awsProfile));
            } else {
                // Use default credentials provider chain (works for both local default profile and ECS)
                builder.credentialsProvider(DefaultCredentialsProvider.create());
            }
        } catch (Exception e) {
            // Fallback to default credentials provider
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        // Always use AWS DynamoDB (no local endpoint override)
        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}