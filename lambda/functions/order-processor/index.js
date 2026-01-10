const mysql = require('mysql2/promise');
const AWS = require('aws-sdk');

const secretsManager = new AWS.SecretsManager({ region: process.env.AWS_REGION || 'us-east-1' });
let connection;
let dbConfig;

async function getDbConfig() {
    if (dbConfig) return dbConfig;
    
    const secretName = process.env.DB_SECRET_NAME || 'ecommerce-db-credentials';
    const secret = await secretsManager.getSecretValue({ SecretId: secretName }).promise();
    const secretData = JSON.parse(secret.SecretString);
    
    dbConfig = {
        host: secretData.host,
        user: secretData.username,
        password: secretData.password,
        database: secretData.database,
        port: secretData.port || 3306
    };
    
    return dbConfig;
}

exports.handler = async (event) => {
    console.log('Processing SQS messages:', JSON.stringify(event, null, 2));
    
    try {
        // Get DB config from Secrets Manager
        const config = await getDbConfig();
        
        // Create connection if not exists
        if (!connection) {
            connection = await mysql.createConnection(config);
        }
        
        for (const record of event.Records) {
            try {
                const orderData = JSON.parse(record.body);
                console.log('Processing order:', orderData.orderId);
                
                // Update order status to PROCESSING
                await updateOrderStatus(orderData.orderId, 'PROCESSING');
                
                // Update payment status to PAID
                await updatePaymentStatus(orderData.orderId, 'PAID');
                
                console.log(`Successfully processed order ${orderData.orderId}`);
                
            } catch (error) {
                console.error('Error processing message:', error);
                throw error;
            }
        }
        
        return { statusCode: 200, body: 'Orders processed successfully' };
        
    } catch (error) {
        console.error('Database connection error:', error);
        throw error;
    }
};

async function updateOrderStatus(orderId, status) {
    const sql = 'UPDATE orders SET status = ?, payment_status = "PAID", updated_at = NOW() WHERE order_id = ?';
    
    await connection.execute(sql, [status, orderId]);
    console.log(`Order ${orderId} status updated to ${status} and payment_status to PAID`);
}

async function updatePaymentStatus(orderId, status) {
    const sql = 'UPDATE payments SET status = ?, updated_at = NOW() WHERE order_id = ?';
    
    await connection.execute(sql, [status, orderId]);
    console.log(`Payment for order ${orderId} status updated to ${status}`);
}