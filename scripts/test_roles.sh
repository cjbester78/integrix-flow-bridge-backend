#!/bin/bash

# Test script for role-based access control

echo "=== Testing Role-Based Access Control ==="
echo

# Function to test endpoint
test_endpoint() {
    local username=$1
    local password=$2
    local endpoint=$3
    local method=${4:-GET}
    local expected_status=$5
    
    # Login and get token
    LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
    
    # Add small delay between requests
    sleep 0.5
    
    if [ "$TOKEN" == "null" ]; then
        echo "❌ Failed to login as $username"
        return
    fi
    
    # Test endpoint
    STATUS=$(curl -s -w "%{http_code}" -o /dev/null -X $method \
        http://localhost:8080$endpoint \
        -H "Authorization: Bearer $TOKEN")
    
    if [ "$STATUS" == "$expected_status" ]; then
        echo "✅ $username - $endpoint ($method) - Expected: $expected_status, Got: $STATUS"
    else
        echo "❌ $username - $endpoint ($method) - Expected: $expected_status, Got: $STATUS"
    fi
}

# Test Administrator
echo "--- Testing Administrator (admin) ---"
test_endpoint "admin" "password123" "/api/system-settings" "GET" "200"
test_endpoint "admin" "password123" "/api/development/functions" "GET" "200"
test_endpoint "admin" "password123" "/api/messages" "GET" "200"
test_endpoint "admin" "password123" "/api/messages/123/reprocess" "POST" "500"  # Will fail but should be authorized

# Test Developer
echo -e "\n--- Testing Developer (testdev) ---"
test_endpoint "testdev" "password123" "/api/system-settings" "GET" "403"
test_endpoint "testdev" "password123" "/api/development/functions" "GET" "200"
test_endpoint "testdev" "password123" "/api/messages" "GET" "200"
test_endpoint "testdev" "password123" "/api/messages/123/reprocess" "POST" "500"  # Will fail but should be authorized

# Test Integrator
echo -e "\n--- Testing Integrator (testint) ---"
test_endpoint "testint" "password123" "/api/system-settings" "GET" "403"
test_endpoint "testint" "password123" "/api/development/functions" "GET" "403"
test_endpoint "testint" "password123" "/api/messages" "GET" "403"
test_endpoint "testint" "password123" "/api/messages/123/reprocess" "POST" "500"  # Will fail but should be authorized

# Test Viewer
echo -e "\n--- Testing Viewer (testview) ---"
test_endpoint "testview" "password123" "/api/system-settings" "GET" "403"
test_endpoint "testview" "password123" "/api/development/functions" "GET" "403"
test_endpoint "testview" "password123" "/api/messages" "GET" "200"
test_endpoint "testview" "password123" "/api/messages/123/reprocess" "POST" "403"

echo -e "\n=== Test Complete ===\n"