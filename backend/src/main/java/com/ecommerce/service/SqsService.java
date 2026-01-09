package com.ecommerce.service;

import com.ecommerce.dto.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.order-processing-queue-url:}")
    private String orderProcessingQueueUrl;

    public void sendOrderProcessingMessage(OrderDto.Response order) {
        try {
            Map<String, Object> messageBody = new HashMap<>();
            messageBody.put("orderId", order.getOrderId());
            messageBody.put("customerId", order.getCustomerId());
            messageBody.put("countryCode", order.getCountryCode());
            messageBody.put("orderStatus", order.getStatus().toString());
            messageBody.put("paymentStatus", order.getPaymentStatus().toString());
            messageBody.put("totalAmount", order.getTotalAmount());
            messageBody.put("customerName", order.getCustomerName());
            messageBody.put("customerEmail", order.getCustomerEmail());
            messageBody.put("orderItems", order.getOrderItems());
            messageBody.put("shippingAddress", order.getShippingAddress());
            messageBody.put("payment", order.getPayment());
            messageBody.put("timestamp", System.currentTimeMillis());
            messageBody.put("eventType", "ORDER_CREATED");

            String messageBodyJson = objectMapper.writeValueAsString(messageBody);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(orderProcessingQueueUrl)
                    .messageBody(messageBodyJson)
                    .build();

            SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
            
            log.info("Order processing message sent to SQS - Order ID: {}, Message ID: {}", 
                    order.getOrderId(), response.messageId());
                    
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message body for order ID: {}", order.getOrderId(), e);
            throw new RuntimeException("Failed to send order processing message", e);
        } catch (Exception e) {
            log.error("Failed to send order processing message for order ID: {}", order.getOrderId(), e);
            throw new RuntimeException("Failed to send order processing message", e);
        }
    }
}