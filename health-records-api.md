# Health Records API Endpoints

This document outlines the API endpoints required for the Midwife Health Records module integration.

## Base URL
```
/api/v1/midwife
```

## Authentication
All endpoints require authentication via Firebase ID token in the Authorization header:
```
Authorization: Bearer <firebase-id-token>
```

---

## 1. Mothers Management

### 1.1 Get Assigned Mothers
**Endpoint:** `GET /assigned-mothers`

**Description:** Retrieve all mothers assigned to the authenticated midwife.

**Request Parameters:** None

**Response:**
```json
[
  {
    "id": "string",
    "name": "string",
    "dueDate": "2024-08-15",
    "phone": "+1234567890",
    "address": "123 Main St, City",
    "gestationalWeek": 24,
    "unit": "string",
    "state": "Active"
  }
]
```

**Status Codes:**
- `200` - Success
- `401` - Unauthorized
- `500` - Internal Server Error

---

### 1.2 Get Mother by ID
**Endpoint:** `GET /mothers/{motherId}`

**Description:** Get basic information for a specific mother.

**Path Parameters:**
- `motherId` (string, required) - The unique identifier of the mother

**Response:**
```json
{
  "id": "string",
  "name": "string",
  "dueDate": "2024-08-15",
  "phone": "+1234567890",
  "address": "123 Main St, City",
  "gestationalWeek": 24,
  "unit": "string",
  "state": "Active"
}
```

**Status Codes:**
- `200` - Success
- `404` - Mother not found
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

### 1.3 Get Mother Health Information
**Endpoint:** `GET /mothers/{motherId}/health`

**Description:** Get comprehensive health information including recent visits.

**Path Parameters:**
- `motherId` (string, required) - The unique identifier of the mother

**Response:**
```json
{
  "mother": {
    "id": "string",
    "name": "string",
    "dueDate": "2024-08-15",
    "phone": "+1234567890",
    "address": "123 Main St, City",
    "gestationalWeek": 24,
    "unit": "string",
    "state": "Active"
  },
  "healthDetails": {
    "id": "string",
    "motherId": "string",
    "age": 28,
    "bloodType": "O+",
    "allergies": "Penicillin",
    "medicalHistory": "Gestational diabetes in previous pregnancy",
    "emergencyContactName": "John Carter",
    "emergencyContactPhone": "+1234567891",
    "registrationDate": "2024-01-15",
    "createdAt": "2024-01-15T10:00:00Z",
    "updatedAt": "2024-01-15T10:00:00Z"
  },
  "recentVisits": [
    {
      "id": "string",
      "motherId": "string",
      "visitDate": "2024-02-15",
      "gestationalWeek": 8,
      "weight": 58.5,
      "bloodPressure": "120/80",
      "glucoseLevel": 95,
      "notes": "Normal development, no complications",
      "createdAt": "2024-02-15T14:30:00Z",
      "updatedAt": "2024-02-15T14:30:00Z"
    }
  ],
  "totalVisits": 3
}
```

**Status Codes:**
- `200` - Success
- `404` - Mother not found
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

## 2. Health Details Management

### 2.1 Get Mother Health Details
**Endpoint:** `GET /mothers/{motherId}/health-details`

**Description:** Get detailed health information for a mother.

**Path Parameters:**
- `motherId` (string, required) - The unique identifier of the mother

**Response:**
```json
{
  "id": "string",
  "motherId": "string",
  "age": 28,
  "bloodType": "O+",
  "allergies": "Penicillin",
  "medicalHistory": "Gestational diabetes in previous pregnancy",
  "emergencyContactName": "John Carter",
  "emergencyContactPhone": "+1234567891",
  "registrationDate": "2024-01-15",
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

**Status Codes:**
- `200` - Success
- `404` - Health details not found
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

### 2.2 Create/Update Mother Health Details
**Endpoint:** `POST /mothers/{motherId}/health-details`

**Description:** Create or update health details for a mother.

**Path Parameters:**
- `motherId` (string, required) - The unique identifier of the mother

**Request Body:**
```json
{
  "age": 28,
  "bloodType": "O+",
  "allergies": "Penicillin",
  "medicalHistory": "Gestational diabetes in previous pregnancy",
  "emergencyContactName": "John Carter",
  "emergencyContactPhone": "+1234567891"
}
```

**Response:**
```json
{
  "id": "string",
  "motherId": "string",
  "age": 28,
  "bloodType": "O+",
  "allergies": "Penicillin",
  "medicalHistory": "Gestational diabetes in previous pregnancy",
  "emergencyContactName": "John Carter",
  "emergencyContactPhone": "+1234567891",
  "registrationDate": "2024-01-15",
  "createdAt": "2024-01-15T10:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

**Status Codes:**
- `200` - Updated successfully
- `201` - Created successfully
- `400` - Invalid request data
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

## 3. Visit Records Management

### 3.1 Get Mother Visit Records
**Endpoint:** `GET /mothers/{motherId}/visits`

**Description:** Get paginated visit records for a mother.

**Path Parameters:**
- `motherId` (string, required) - The unique identifier of the mother

**Query Parameters:**
- `page` (integer, optional) - Page number (default: 0)
- `size` (integer, optional) - Page size (default: 20)
- `fromDate` (string, optional) - Filter visits from date (ISO format)
- `toDate` (string, optional) - Filter visits to date (ISO format)

**Response:**
```json
{
  "content": [
    {
      "id": "string",
      "motherId": "string",
      "visitDate": "2024-02-15",
      "gestationalWeek": 8,
      "weight": 58.5,
      "bloodPressure": "120/80",
      "glucoseLevel": 95,
      "notes": "Normal development, no complications",
      "createdAt": "2024-02-15T14:30:00Z",
      "updatedAt": "2024-02-15T14:30:00Z"
    }
  ],
  "totalElements": 5,
  "totalPages": 1,
  "page": 0,
  "size": 20
}
```

**Status Codes:**
- `200` - Success
- `404` - Mother not found
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

### 3.2 Get Latest Visit
**Endpoint:** `GET /mothers/{motherId}/visits/latest`

**Description:** Get the most recent visit record for a mother.

**Path Parameters:**
- `motherId` (string, required) - The unique identifier of the mother

**Response:**
```json
{
  "id": "string",
  "motherId": "string",
  "visitDate": "2024-02-15",
  "gestationalWeek": 8,
  "weight": 58.5,
  "bloodPressure": "120/80",
  "glucoseLevel": 95,
  "notes": "Normal development, no complications",
  "createdAt": "2024-02-15T14:30:00Z",
  "updatedAt": "2024-02-15T14:30:00Z"
}
```

**Status Codes:**
- `200` - Success
- `404` - No visits found for mother
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

### 3.3 Create Visit Record
**Endpoint:** `POST /visits`

**Description:** Create a new visit record.

**Request Body:**
```json
{
  "motherId": "string",
  "gestationalWeek": 8,
  "weight": 58.5,
  "bloodPressure": "120/80",
  "glucoseLevel": 95,
  "notes": "Normal development, no complications"
}
```

**Response:**
```json
{
  "id": "string",
  "motherId": "string",
  "visitDate": "2024-02-15",
  "gestationalWeek": 8,
  "weight": 58.5,
  "bloodPressure": "120/80",
  "glucoseLevel": 95,
  "notes": "Normal development, no complications",
  "createdAt": "2024-02-15T14:30:00Z",
  "updatedAt": "2024-02-15T14:30:00Z"
}
```

**Status Codes:**
- `201` - Created successfully
- `400` - Invalid request data
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

### 3.4 Get Visit Record by ID
**Endpoint:** `GET /visits/{visitId}`

**Description:** Get a specific visit record by its ID.

**Path Parameters:**
- `visitId` (string, required) - The unique identifier of the visit

**Response:**
```json
{
  "id": "string",
  "motherId": "string",
  "visitDate": "2024-02-15",
  "gestationalWeek": 8,
  "weight": 58.5,
  "bloodPressure": "120/80",
  "glucoseLevel": 95,
  "notes": "Normal development, no complications",
  "createdAt": "2024-02-15T14:30:00Z",
  "updatedAt": "2024-02-15T14:30:00Z"
}
```

**Status Codes:**
- `200` - Success
- `404` - Visit not found
- `401` - Unauthorized
- `403` - Visit not accessible to this midwife

---

### 3.5 Update Visit Record
**Endpoint:** `PUT /visits/{visitId}`

**Description:** Update an existing visit record.

**Path Parameters:**
- `visitId` (string, required) - The unique identifier of the visit

**Request Body:**
```json
{
  "weight": 59.0,
  "bloodPressure": "118/78",
  "glucoseLevel": 92,
  "notes": "Updated notes after consultation"
}
```

**Response:**
```json
{
  "id": "string",
  "motherId": "string",
  "visitDate": "2024-02-15",
  "gestationalWeek": 8,
  "weight": 59.0,
  "bloodPressure": "118/78",
  "glucoseLevel": 92,
  "notes": "Updated notes after consultation",
  "createdAt": "2024-02-15T14:30:00Z",
  "updatedAt": "2024-02-15T16:45:00Z"
}
```

**Status Codes:**
- `200` - Updated successfully
- `400` - Invalid request data
- `404` - Visit not found
- `401` - Unauthorized
- `403` - Visit not accessible to this midwife

---

### 3.6 Delete Visit Record
**Endpoint:** `DELETE /visits/{visitId}`

**Description:** Delete a visit record.

**Path Parameters:**
- `visitId` (string, required) - The unique identifier of the visit

**Response:** No content

**Status Codes:**
- `204` - Deleted successfully
- `404` - Visit not found
- `401` - Unauthorized
- `403` - Visit not accessible to this midwife

---

## 4. Statistics and Analytics

### 4.1 Get Visit Statistics
**Endpoint:** `GET /mothers/{motherId}/visits/statistics`

**Description:** Get visit statistics and analytics for a mother.

**Path Parameters:**
- `motherId` (string, required) - The unique identifier of the mother

**Response:**
```json
{
  "totalVisits": 5,
  "lastVisitDate": "2024-02-15",
  "averageWeight": 59.2,
  "weightTrend": "increasing"
}
```

**Status Codes:**
- `200` - Success
- `404` - Mother not found
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

### 4.2 Get Recommended Visit Schedule
**Endpoint:** `GET /mothers/{motherId}/visit-schedule`

**Description:** Get recommended visit schedule based on gestational week and risk factors.

**Path Parameters:**
- `motherId` (string, required) - The unique identifier of the mother

**Response:**
```json
{
  "nextRecommendedDate": "2024-03-15",
  "visitFrequency": "Every 4 weeks",
  "totalRecommendedVisits": 14
}
```

**Status Codes:**
- `200` - Success
- `404` - Mother not found
- `401` - Unauthorized
- `403` - Mother not assigned to this midwife

---

## Data Types and Enums

### Mother State Values
- `Active` - Mother is actively under care
- `Inactive` - Mother is temporarily not receiving care
- `Completed` - Pregnancy completed (delivery occurred)
- `Pending` - Initial registration, care not yet started

### Blood Type Values
- `A+`, `A-`, `B+`, `B-`, `AB+`, `AB-`, `O+`, `O-`

### Weight Trend Values
- `increasing` - Weight trending upward
- `decreasing` - Weight trending downward
- `stable` - Weight relatively stable

---

## Error Response Format

All error responses follow this format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error message",
    "details": "Additional error details if available",
    "timestamp": "2024-02-15T14:30:00Z"
  }
}
```

### Common Error Codes
- `UNAUTHORIZED` - Authentication required
- `FORBIDDEN` - Access denied
- `NOT_FOUND` - Resource not found
- `VALIDATION_ERROR` - Invalid request data
- `INTERNAL_ERROR` - Server error

---

## Notes for Backend Implementation

1. **Authentication**: All endpoints should validate the Firebase ID token and extract the midwife's identity.

2. **Authorization**: Ensure midwives can only access mothers assigned to them.

3. **Data Validation**: Implement proper validation for all input fields, especially medical data.

4. **Audit Trail**: Consider implementing audit logging for all health record modifications.

5. **Pagination**: Use consistent pagination for all list endpoints.

6. **Date Handling**: All dates should be in ISO 8601 format (YYYY-MM-DD or full timestamp).

7. **Error Handling**: Provide meaningful error messages while avoiding sensitive information leakage.

8. **Performance**: Consider implementing caching for frequently accessed data like mother lists.

9. **Backup**: Ensure critical health data is properly backed up and recoverable.

10. **Compliance**: Ensure all endpoints comply with healthcare data privacy regulations (HIPAA, etc.).