package com.ecommerce.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Service
@Slf4j
public class SecretsManagerService {

    private final SecretsManagerClient secretsManagerClient;
    private final ObjectMapper objectMapper;
    private final String dbSecretName;

    public SecretsManagerService(@Value("${aws.region}") String region,
                                @Value("${aws.secrets.db-secret-name}") String dbSecretName) {
        this.secretsManagerClient = SecretsManagerClient.builder()
                .region(Region.of(region))
                .build();
        this.objectMapper = new ObjectMapper();
        this.dbSecretName = dbSecretName;
    }

    public String getDatabasePassword() {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(dbSecretName)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String secretString = response.secretString();

            JsonNode secretJson = objectMapper.readTree(secretString);
            return secretJson.get("password").asText();
        } catch (Exception e) {
            log.error("Failed to retrieve database password from Secrets Manager", e);
            throw new RuntimeException("Failed to retrieve database credentials", e);
        }
    }

    public String getDatabaseUsername() {
        try {
            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(dbSecretName)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String secretString = response.secretString();

            JsonNode secretJson = objectMapper.readTree(secretString);
            return secretJson.get("username").asText();
        } catch (Exception e) {
            log.error("Failed to retrieve database username from Secrets Manager", e);
            throw new RuntimeException("Failed to retrieve database credentials", e);
        }
    }
}