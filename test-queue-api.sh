#!/bin/bash

# Queue Management System Test Script
# This script tests the queue management endpoints

BASE_URL="http://localhost:8080/api/v1/moh/clinics"
CLINIC_ID="your-clinic-id-here"  # Replace with actual clinic ID
AUTHORIZATION_HEADER="Authorization: Bearer YOUR_JWT_TOKEN_HERE"  # Replace with actual JWT token

echo "🚀 Testing Queue Management System API"
echo "========================================"

# Test 1: Start Queue
echo "1. Starting queue..."
curl -X POST "${BASE_URL}/${CLINIC_ID}/queue/start" \
  -H "Content-Type: application/json" \
  -H "${AUTHORIZATION_HEADER}" \
  -d '{
    "maxCapacity": 50,
    "avgAppointmentTime": 15
  }' \
  -w "\nStatus: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 2: Add Users to Queue
echo "2. Adding users to queue..."
curl -X POST "${BASE_URL}/${CLINIC_ID}/queue/add-users" \
  -H "Content-Type: application/json" \
  -H "${AUTHORIZATION_HEADER}" \
  -d '{
    "users": [
      {
        "name": "Sarah Johnson",
        "email": "sarah.johnson@email.com"
      },
      {
        "name": "Emily Davis",
        "email": "emily.davis@email.com"
      },
      {
        "name": "Michael Brown",
        "email": "michael.brown@email.com"
      }
    ]
  }' \
  -w "\nStatus: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 3: Get Queue Status
echo "3. Getting queue status..."
curl -X GET "${BASE_URL}/${CLINIC_ID}/queue" \
  -H "${AUTHORIZATION_HEADER}" \
  -w "\nStatus: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 4: Get Queue Statistics
echo "4. Getting queue statistics..."
curl -X GET "${BASE_URL}/${CLINIC_ID}/queue/statistics" \
  -H "${AUTHORIZATION_HEADER}" \
  -w "\nStatus: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 5: Complete Current Appointment
echo "5. Completing current appointment..."
curl -X POST "${BASE_URL}/${CLINIC_ID}/queue/complete" \
  -H "${AUTHORIZATION_HEADER}" \
  -w "\nStatus: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 6: Get Updated Queue Status
echo "6. Getting updated queue status..."
curl -X GET "${BASE_URL}/${CLINIC_ID}/queue" \
  -H "${AUTHORIZATION_HEADER}" \
  -w "\nStatus: %{http_code}\n" \
  -s

echo -e "\n---\n"

# Test 7: Close Queue
echo "7. Closing queue..."
curl -X POST "${BASE_URL}/${CLINIC_ID}/queue/close" \
  -H "${AUTHORIZATION_HEADER}" \
  -w "\nStatus: %{http_code}\n" \
  -s

echo -e "\n✅ Queue Management System Test Complete!"
echo "========================================"
