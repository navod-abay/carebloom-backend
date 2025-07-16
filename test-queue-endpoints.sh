#!/bin/bash

# Test script for Queue Management System endpoints
# Base URL for the MoH clinic endpoints
BASE_URL="http://localhost:8082/api/v1/moh"

echo "=== Testing Queue Management System Endpoints ==="
echo

# Test 1: Get queue status (should return 404 for non-existent queue)
echo "1. Testing GET queue status for clinic 'test-clinic-id':"
curl -s -X GET "$BASE_URL/clinics/test-clinic-id/queue/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -w "\nHTTP Status: %{http_code}\n" || echo "Request failed"

echo
echo "---"
echo

# Test 2: Start queue (should work)
echo "2. Testing POST start queue for clinic 'test-clinic-id':"
curl -s -X POST "$BASE_URL/clinics/test-clinic-id/queue/start" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "expectedPatients": 10,
    "estimatedTimePerPatient": 15
  }' \
  -w "\nHTTP Status: %{http_code}\n" || echo "Request failed"

echo
echo "---"
echo

# Test 3: Check if endpoints are reachable (basic connectivity test)
echo "3. Testing basic connectivity to server:"
curl -s -X GET "http://localhost:8082/actuator/health" \
  -w "\nHTTP Status: %{http_code}\n" 2>/dev/null || echo "Health endpoint not available"

echo
echo "=== Test completed ==="
