# CareBloom Queue Management API

## Overview
The CareBloom Queue Management System provides real-time clinic queue operations for maternity care appointments. This API supports queue initialization, patient management, and live updates via Server-Sent Events (SSE).

## Base URL
```
/api/clinics/{clinicId}/queue
```

## Endpoints

### 1. Start Queue
**POST** `/start`

Initializes a queue with the clinic's registered mothers.

**Response:**
```json
{
  "success": true,
  "data": {
    "clinicId": "clinic-123",
    "isActive": true,
    "currentPatient": {
      "name": "Sarah Johnson",
      "email": "sarah@example.com",
      "position": 1,
      "status": "in-progress"
    },
    "waitingQueue": [
      {
        "name": "Emma Wilson",
        "position": 2,
        "estimatedWaitTime": 15,
        "status": "waiting"
      }
    ],
    "stats": {
      "totalPatients": 10,
      "completed": 0,
      "waiting": 9,
      "inProgress": 1
    }
  }
}
```

### 2. Get Queue Status
**GET** `/`

Retrieves current queue status and patient information.

### 3. Close Queue
**POST** `/close?force=false`

Closes the queue. If patients are still waiting, requires confirmation unless `force=true`.

**Response (with confirmation required):**
```json
{
  "requiresConfirmation": true,
  "waitingPatients": 5,
  "inProgressPatients": 1,
  "message": "There are still patients in the queue. Do you want to force close?"
}
```

### 4. Process Next Patient
**POST** `/process-next`

Marks current patient as completed and moves next patient to in-progress.

### 5. Add Patient Manually
**POST** `/add`

**Request:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "motherId": "mother-456",
  "notes": "Emergency appointment"
}
```

### 6. Update Patient Status
**PUT** `/patients/{patientId}/status`

**Request:**
```json
{
  "status": "completed"
}
```

Valid statuses: `waiting`, `in-progress`, `completed`, `no-show`

### 7. Remove Patient
**DELETE** `/patients/{patientId}`

Removes patient from queue and reorders remaining positions.

### 8. Reorder Queue
**POST** `/reorder`

**Request:**
```json
{
  "patientIds": ["patient1", "patient2", "patient3"]
}
```

### 9. Update Queue Settings
**PUT** `/settings`

**Request:**
```json
{
  "isOpen": true,
  "maxCapacity": 50,
  "avgAppointmentTime": 20,
  "closingTime": "17:00",
  "autoClose": true
}
```

### 10. Real-time Updates (SSE)
**GET** `/events`

Subscribe to Server-Sent Events for real-time queue updates.

**Usage:**
```javascript
const eventSource = new EventSource('/api/clinics/clinic-123/queue/events');

eventSource.onmessage = function(event) {
  const queueData = JSON.parse(event.data);
  // Update UI with queue changes
};
```

## Mother Management Endpoints

### Add Mothers to Clinic
**POST** `/api/v1/moh/clinics/{id}/mothers`

**Request:**
```json
["mother-id-1", "mother-id-2", "mother-id-3"]
```

### Remove Mother from Clinic
**DELETE** `/api/v1/moh/clinics/{id}/mothers/{motherId}`

## Error Handling

All endpoints return standardized error responses:

```json
{
  "success": false,
  "error": "Error message description"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Successful operation
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `409 Conflict` - Invalid state (e.g., queue already started)
- `500 Internal Server Error` - Server error

## Business Rules

1. **Queue Initialization:**
   - Can only start queue on clinic date
   - Requires mothers to be added to clinic
   - Auto-sets first patient as "in-progress"

2. **Queue Processing:**
   - Only one patient can be "in-progress" at a time
   - Wait times calculated based on position and average appointment duration
   - Estimated times updated dynamically

3. **Queue Validation:**
   - Enforces maximum capacity limits
   - Prevents duplicate patient additions
   - Validates clinic ownership and permissions

4. **Real-time Updates:**
   - SSE broadcasts sent on all queue state changes
   - Automatic reconnection handling
   - Graceful error handling for disconnected clients

## Performance Considerations

- Queue operations are optimized for concurrent access
- Database queries use proper indexing for fast lookups
- SSE connections are managed efficiently with automatic cleanup
- Real-time updates have minimal latency (< 100ms typically)
