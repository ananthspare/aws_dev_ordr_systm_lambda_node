const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'your-secret-key';

exports.handler = async (event) => {
    console.log('Authorizer event:', JSON.stringify(event, null, 2));
    
    try {
        const token = extractToken(event);
        if (!token) {
            throw new Error('No token provided');
        }

        const decoded = jwt.verify(token, JWT_SECRET);
        console.log('Token decoded successfully:', decoded);

        const policy = generatePolicy(decoded.sub, 'Allow', event.methodArn, {
            customerId: decoded.customerId,
            email: decoded.email,
            name: decoded.name
        });

        console.log('Generated policy:', JSON.stringify(policy, null, 2));
        return policy;

    } catch (error) {
        console.error('Authorization failed:', error.message);
        
        // Return deny policy for invalid tokens
        return generatePolicy('user', 'Deny', event.methodArn);
    }
};

function extractToken(event) {
    // Try to get token from Authorization header
    const authHeader = event.headers?.Authorization || event.headers?.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
        return authHeader.substring(7);
    }
    
    // Try to get token from query parameters
    if (event.queryStringParameters?.token) {
        return event.queryStringParameters.token;
    }
    
    return null;
}

function generatePolicy(principalId, effect, resource, context = {}) {
    const policy = {
        principalId: principalId,
        policyDocument: {
            Version: '2012-10-17',
            Statement: [
                {
                    Action: 'execute-api:Invoke',
                    Effect: effect,
                    Resource: resource
                }
            ]
        }
    };

    // Add context if provided (will be available in backend as headers)
    if (Object.keys(context).length > 0) {
        policy.context = context;
    }

    return policy;
}