#!/bin/bash

# Script to test deletion constraints in Integrix Flow Bridge
# This script will create entities and then try to delete them to verify constraints work

API_BASE="http://localhost:8080/api"
TOKEN=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🔐 Testing Deletion Constraints for Integrix Flow Bridge"
echo "========================================================"

# Function to login and get JWT token
login() {
    echo -e "\n${YELLOW}1. Logging in...${NC}"
    response=$(curl -s -X POST "$API_BASE/auth/login" \
        -H "Content-Type: application/json" \
        -d '{
            "username": "admin",
            "password": "admin123"
        }')
    
    TOKEN=$(echo $response | grep -o '"token":"[^"]*' | sed 's/"token":"//')
    
    if [ -z "$TOKEN" ]; then
        echo -e "${RED}❌ Login failed! Please check credentials.${NC}"
        exit 1
    else
        echo -e "${GREEN}✅ Login successful!${NC}"
    fi
}

# Function to create a message structure
create_message_structure() {
    echo -e "\n${YELLOW}2. Creating Message Structure...${NC}"
    response=$(curl -s -X POST "$API_BASE/message-structures" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Message Structure for Deletion",
            "description": "This message structure will be referenced by a flow structure",
            "xsdContent": "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><xsd:element name=\"test\"/></xsd:schema>",
            "sourceType": "INTERNAL"
        }')
    
    MESSAGE_STRUCTURE_ID=$(echo $response | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
    
    if [ -z "$MESSAGE_STRUCTURE_ID" ]; then
        echo -e "${RED}❌ Failed to create message structure!${NC}"
        echo "Response: $response"
        exit 1
    else
        echo -e "${GREEN}✅ Message Structure created with ID: $MESSAGE_STRUCTURE_ID${NC}"
    fi
}

# Function to create a flow structure that references the message structure
create_flow_structure() {
    echo -e "\n${YELLOW}3. Creating Flow Structure...${NC}"
    response=$(curl -s -X POST "$API_BASE/flow-structures" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Flow Structure for Deletion",
            "description": "This flow structure references a message structure",
            "wsdlContent": "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"></wsdl:definitions>",
            "processingMode": "SYNC",
            "direction": "SOURCE",
            "messageStructureIds": ["'$MESSAGE_STRUCTURE_ID'"]
        }')
    
    FLOW_STRUCTURE_ID=$(echo $response | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
    
    if [ -z "$FLOW_STRUCTURE_ID" ]; then
        echo -e "${RED}❌ Failed to create flow structure!${NC}"
        echo "Response: $response"
        exit 1
    else
        echo -e "${GREEN}✅ Flow Structure created with ID: $FLOW_STRUCTURE_ID${NC}"
    fi
}

# Function to create an integration flow that references the flow structure
create_integration_flow() {
    echo -e "\n${YELLOW}4. Creating Integration Flow...${NC}"
    
    # First, we need to create adapters
    # Create source adapter
    echo -e "${YELLOW}   Creating source adapter...${NC}"
    response=$(curl -s -X POST "$API_BASE/adapters" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Source Adapter",
            "type": "HTTP",
            "mode": "SENDER",
            "configuration": {
                "url": "http://example.com",
                "method": "GET"
            }
        }')
    
    SOURCE_ADAPTER_ID=$(echo $response | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
    
    # Create target adapter
    echo -e "${YELLOW}   Creating target adapter...${NC}"
    response=$(curl -s -X POST "$API_BASE/adapters" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Target Adapter",
            "type": "HTTP",
            "mode": "RECEIVER",
            "configuration": {
                "url": "http://example.com",
                "method": "POST"
            }
        }')
    
    TARGET_ADAPTER_ID=$(echo $response | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
    
    # Create integration flow
    response=$(curl -s -X POST "$API_BASE/integration-flows" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "name": "Test Integration Flow",
            "description": "This flow references flow structures",
            "sourceAdapterId": "'$SOURCE_ADAPTER_ID'",
            "targetAdapterId": "'$TARGET_ADAPTER_ID'",
            "sourceFlowStructureId": "'$FLOW_STRUCTURE_ID'",
            "targetFlowStructureId": "'$FLOW_STRUCTURE_ID'",
            "flowType": "DIRECT_MAPPING",
            "mappingMode": "PASS_THROUGH"
        }')
    
    INTEGRATION_FLOW_ID=$(echo $response | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
    
    if [ -z "$INTEGRATION_FLOW_ID" ]; then
        echo -e "${RED}❌ Failed to create integration flow!${NC}"
        echo "Response: $response"
        exit 1
    else
        echo -e "${GREEN}✅ Integration Flow created with ID: $INTEGRATION_FLOW_ID${NC}"
    fi
}

# Function to create a transformation with field mappings
create_transformation_with_mappings() {
    echo -e "\n${YELLOW}5. Creating Transformation with Field Mappings...${NC}"
    
    # Create transformation
    response=$(curl -s -X POST "$API_BASE/flow-transformations" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "flowId": "'$INTEGRATION_FLOW_ID'",
            "name": "Test Transformation",
            "type": "FIELD_MAPPING",
            "configuration": "{}",
            "executionOrder": 1
        }')
    
    TRANSFORMATION_ID=$(echo $response | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
    
    if [ -z "$TRANSFORMATION_ID" ]; then
        echo -e "${RED}❌ Failed to create transformation!${NC}"
        echo "Response: $response"
        exit 1
    else
        echo -e "${GREEN}✅ Transformation created with ID: $TRANSFORMATION_ID${NC}"
    fi
    
    # Create field mapping
    echo -e "${YELLOW}   Creating field mapping...${NC}"
    response=$(curl -s -X POST "$API_BASE/field-mappings" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "transformationId": "'$TRANSFORMATION_ID'",
            "sourceFields": ["sourceField1"],
            "targetField": "targetField1",
            "mappingOrder": 1
        }')
    
    FIELD_MAPPING_ID=$(echo $response | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
    
    if [ -z "$FIELD_MAPPING_ID" ]; then
        echo -e "${RED}❌ Failed to create field mapping!${NC}"
        echo "Response: $response"
    else
        echo -e "${GREEN}✅ Field Mapping created with ID: $FIELD_MAPPING_ID${NC}"
    fi
}

# Test deletion constraints
test_deletion_constraints() {
    echo -e "\n${YELLOW}6. Testing Deletion Constraints...${NC}"
    echo "================================================"
    
    # Test 1: Try to delete message structure (should fail - referenced by flow structure)
    echo -e "\n${YELLOW}Test 1: Attempting to delete Message Structure (should fail)...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$API_BASE/message-structures/$MESSAGE_STRUCTURE_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 400 ] || [ "$http_code" -eq 409 ]; then
        echo -e "${GREEN}✅ PASS: Message Structure deletion blocked as expected!${NC}"
        echo "   Error: $body"
    else
        echo -e "${RED}❌ FAIL: Message Structure deletion should have been blocked!${NC}"
        echo "   HTTP Code: $http_code"
        echo "   Response: $body"
    fi
    
    # Test 2: Try to delete flow structure (should fail - referenced by integration flow)
    echo -e "\n${YELLOW}Test 2: Attempting to delete Flow Structure (should fail)...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$API_BASE/flow-structures/$FLOW_STRUCTURE_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 400 ] || [ "$http_code" -eq 409 ]; then
        echo -e "${GREEN}✅ PASS: Flow Structure deletion blocked as expected!${NC}"
        echo "   Error: $body"
    else
        echo -e "${RED}❌ FAIL: Flow Structure deletion should have been blocked!${NC}"
        echo "   HTTP Code: $http_code"
        echo "   Response: $body"
    fi
    
    # Test 3: Try to delete transformation (should fail - has field mappings)
    echo -e "\n${YELLOW}Test 3: Attempting to delete Transformation (should fail)...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$API_BASE/flow-transformations/$TRANSFORMATION_ID" \
        -H "Authorization: Bearer $TOKEN")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 400 ] || [ "$http_code" -eq 409 ]; then
        echo -e "${GREEN}✅ PASS: Transformation deletion blocked as expected!${NC}"
        echo "   Error: $body"
    else
        echo -e "${RED}❌ FAIL: Transformation deletion should have been blocked!${NC}"
        echo "   HTTP Code: $http_code"
        echo "   Response: $body"
    fi
    
    # Test 4: Delete in correct order (should succeed)
    echo -e "\n${YELLOW}Test 4: Deleting entities in correct order...${NC}"
    
    # Delete field mapping first
    echo -e "${YELLOW}   Deleting field mapping...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$API_BASE/field-mappings/$FIELD_MAPPING_ID" \
        -H "Authorization: Bearer $TOKEN")
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 204 ]; then
        echo -e "${GREEN}   ✅ Field mapping deleted successfully${NC}"
    else
        echo -e "${RED}   ❌ Failed to delete field mapping${NC}"
    fi
    
    # Delete integration flow
    echo -e "${YELLOW}   Deleting integration flow...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$API_BASE/integration-flows/$INTEGRATION_FLOW_ID" \
        -H "Authorization: Bearer $TOKEN")
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 204 ]; then
        echo -e "${GREEN}   ✅ Integration flow deleted successfully${NC}"
    else
        echo -e "${RED}   ❌ Failed to delete integration flow${NC}"
    fi
    
    # Now try to delete flow structure (should succeed)
    echo -e "${YELLOW}   Deleting flow structure...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$API_BASE/flow-structures/$FLOW_STRUCTURE_ID" \
        -H "Authorization: Bearer $TOKEN")
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 204 ]; then
        echo -e "${GREEN}   ✅ Flow structure deleted successfully${NC}"
    else
        echo -e "${RED}   ❌ Failed to delete flow structure${NC}"
    fi
    
    # Finally delete message structure (should succeed)
    echo -e "${YELLOW}   Deleting message structure...${NC}"
    response=$(curl -s -w "\n%{http_code}" -X DELETE "$API_BASE/message-structures/$MESSAGE_STRUCTURE_ID" \
        -H "Authorization: Bearer $TOKEN")
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 204 ]; then
        echo -e "${GREEN}   ✅ Message structure deleted successfully${NC}"
    else
        echo -e "${RED}   ❌ Failed to delete message structure${NC}"
    fi
}

# Main execution
main() {
    # Check if API is running
    if ! curl -s -f "$API_BASE/actuator/health" > /dev/null; then
        echo -e "${RED}❌ API is not running at $API_BASE${NC}"
        echo "Please ensure the application is running before running this test."
        exit 1
    fi
    
    # Run tests
    login
    create_message_structure
    create_flow_structure
    create_integration_flow
    create_transformation_with_mappings
    test_deletion_constraints
    
    echo -e "\n${GREEN}✅ Deletion constraint tests completed!${NC}"
    echo "========================================"
}

# Run the main function
main