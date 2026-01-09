const AWS = require('aws-sdk');

// Initialize AWS services
const dynamodb = new AWS.DynamoDB.DocumentClient();
const eventbridge = new AWS.EventBridge();

exports.handler = async (event) => {
    console.log('Order processor Lambda triggered with event:', JSON.stringify(event, null, 2));
    
    const results = [];
    
    // Process each SQS record
    for (const record of event.Records) {
        try {
            const messageBody = JSON.parse(record.body);
            console.log('Processing order message:', messageBody);
            
            const result = await processOrder(messageBody);
            results.push({
                messageId: record.messageId,
                status: 'SUCCESS',
                result: result
            });
            
        } catch (error) {
            console.error('Error processing message:', record.messageId, error);
            results.push({
                messageId: record.messageId,
                status: 'ERROR',
                error: error.message
            });
        }
    }
    
    return {
        statusCode: 200,
        body: JSON.stringify({
            message: 'Order processing completed',
            results: results
        })
    };
};

async function processOrder(orderMessage) {
    const { orderId, customerId, countryCode, eventType } = orderMessage;
    
    console.log(`Processing order ${orderId} for customer ${customerId} in country ${countryCode}`);
    
    // TODO: Implement order processing logic
    // This is where you would:
    // 1. Update order status in DynamoDB
    // 2. Generate invoice
    // 3. Send notifications
    // 4. Trigger shipping workflows via EventBridge
    
    // For now, just log the processing
    console.log(`Order ${orderId} processed successfully`);
    
    // Example: Send event to EventBridge for further processing
    await sendOrderProcessedEvent(orderId, customerId, countryCode);
    
    return {
        orderId: orderId,
        status: 'PROCESSED',
        timestamp: new Date().toISOString()
    };
}

async function sendOrderProcessedEvent(orderId, customerId, countryCode) {
    const eventDetail = {
        orderId: orderId,
        customerId: customerId,
        countryCode: countryCode,
        status: 'PROCESSED',
        timestamp: new Date().toISOString()
    };
    
    const params = {
        Entries: [
            {
                Source: 'ecommerce.order-processor',
                DetailType: 'Order Processed',
                Detail: JSON.stringify(eventDetail),
                EventBusName: 'default' // or your custom event bus
            }
        ]
    };
    
    try {
        const result = await eventbridge.putEvents(params).promise();
        console.log('Event sent to EventBridge:', result);
    } catch (error) {
        console.error('Failed to send event to EventBridge:', error);
        throw error;
    }
}