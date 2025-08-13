const WebSocket = require('ws');

console.log('WebSocket Connection Test');
console.log('========================\n');

// Test endpoints
const endpoints = [
    '/ws/messages',
    '/ws/test', 
    '/ws/flow-execution'
];

function testEndpoint(endpoint) {
    return new Promise((resolve) => {
        const url = `ws://localhost:8080${endpoint}`;
        console.log(`Testing ${url}...`);
        
        const ws = new WebSocket(url, {
            headers: {
                'Origin': 'http://localhost:8080'
            }
        });
        
        ws.on('open', () => {
            console.log(`✅ SUCCESS: Connected to ${endpoint}`);
            
            // Send test message
            if (endpoint === '/ws/messages') {
                ws.send(JSON.stringify({ command: 'get_stats' }));
            } else {
                ws.send('Hello WebSocket!');
            }
            
            setTimeout(() => {
                ws.close();
                resolve(true);
            }, 1000);
        });
        
        ws.on('message', (data) => {
            console.log(`📨 Received from ${endpoint}:`, data.toString());
        });
        
        ws.on('error', (error) => {
            console.log(`❌ ERROR on ${endpoint}:`, error.message);
            resolve(false);
        });
        
        ws.on('close', (code, reason) => {
            console.log(`🔌 Closed ${endpoint}: Code=${code}, Reason=${reason}`);
            console.log('---\n');
        });
        
        // Timeout after 5 seconds
        setTimeout(() => {
            if (ws.readyState !== WebSocket.CLOSED) {
                ws.close();
                resolve(false);
            }
        }, 5000);
    });
}

async function runTests() {
    console.log('Starting WebSocket tests...\n');
    
    for (const endpoint of endpoints) {
        await testEndpoint(endpoint);
    }
    
    console.log('Tests complete!');
}

// Check if ws module is installed
try {
    require.resolve('ws');
    runTests();
} catch(e) {
    console.log('Please install the ws module first:');
    console.log('npm install ws');
}