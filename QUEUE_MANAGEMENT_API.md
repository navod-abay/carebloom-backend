# Queue Management System API Documentation

## Overview
This queue management system provides real-time queue operations for clinic appointments. The system integrates with the existing clinic management infrastructure and provides endpoints for managing appointment queues.

## Features
- ✅ Start/Stop queue for clinics
- ✅ Add users to queue (bulk operations)
- ✅ Complete appointments (move queue forward)
- ✅ Real-time queue status and statistics
- ✅ Wait time calculations
- ✅ WebSocket notifications (via QueueNotificationService)
- ✅ Proper authentication and authorization
- ✅ Data persistence across server restarts

## API Endpoints

### Base URL
```
/api/v1/moh/clinics/{clinicId}/queue
```

### 1. Start Queue
**POST** `/api/v1/moh/clinics/{clinicId}/queue/start`

Initialize a new queue for a clinic.

**Request Body:**
```json
{
  "maxCapacity": 50,
  "avgAppointmentTime": 15
}
```

**Response:**
```json
{
  "success": true,
  "message": "Queue started successfully",
  "data": {
    "id": "queue-uuid",
    "clinicId": "clinic-uuid",
    "status": "active",
    "maxCapacity": 50,
    "avgAppointmentTime": 15,
    "completedAppointments": 0
  }
}
```

### 2. Add Users to Queue
**POST** `/api/v1/moh/clinics/{clinicId}/queue/add-users`

Add multiple users to the queue.

**Request Body:**
```json
{
  "users": [
    {
      "name": "Sarah Johnson",
      "email": "sarah.johnson@email.com"
    },
    {
      "name": "Emily Davis",
      "email": "emily.davis@email.com"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Users added to queue successfully"
}
```

### 3. Complete Current Appointment
**POST** `/api/v1/moh/clinics/{clinicId}/queue/complete`

Complete the current appointment and move the queue forward.

**Response:**
```json
{
  "success": true,
  "message": "Appointment completed successfully"
}
```

### 4. Close Queue
**POST** `/api/v1/moh/clinics/{clinicId}/queue/close`

Close the queue (no more appointments).

**Response:**
```json
{
  "success": true,
  "message": "Queue closed successfully"
}
```

### 5. Get Queue Status
**GET** `/api/v1/moh/clinics/{clinicId}/queue`

Get the current queue status with all entries.

**Response:**
```json
{
  "success": true,
  "message": "Queue retrieved successfully",
  "data": {
    "id": "queue-uuid",
    "clinicId": "clinic-uuid",
    "status": "active",
    "settings": {
      "maxCapacity": 50,
      "avgAppointmentTime": 15
    },
    "currentUser": {
      "name": "Sarah Johnson",
      "email": "sarah.johnson@email.com",
      "waitTime": 0,
      "joinedTime": "2025-07-14T10:30:00",
      "queuePosition": 1,
      "status": "waiting"
    },
    "waitingUsers": [
      {
        "name": "Emily Davis",
        "email": "emily.davis@email.com",
        "waitTime": 15,
        "joinedTime": "2025-07-14T10:35:00",
        "queuePosition": 2,
        "status": "waiting"
      }
    ],
    "completedCount": 3,
    "statistics": {
      "totalWaitTime": 420,
      "avgWaitTime": 12,
      "queueLength": 8
    }
  }
}
```

### 6. Get Queue Statistics
**GET** `/api/v1/moh/clinics/{clinicId}/queue/statistics`

Get queue statistics and summary information.

**Response:**
```json
{
  "success": true,
  "data": {
    "completedAppointments": 3,
    "currentQueueLength": 8,
    "averageWaitTime": 12,
    "queueStatus": "active",
    "estimatedCompletionTime": "2025-07-14T16:45:00"
  }
}
```

## Business Logic

### Queue Flow
1. **Start Queue**: Initialize queue with capacity and average appointment time
2. **Add Users**: Bulk add users to queue with automatic position assignment
3. **Complete Appointment**: Mark current user as completed, move queue forward
4. **Close Queue**: End queue management session

### Wait Time Calculation
- **Formula**: `position * avgAppointmentTime`
- **Current User**: Always 0 minutes wait time
- **Waiting Users**: Calculated based on their position in queue

### Queue Position Management
- Positions are automatically assigned sequentially
- When an appointment is completed, all subsequent positions shift down
- Wait times are recalculated after each completion

### Error Handling
- **404**: Clinic not found or no access
- **400**: Invalid request (queue already exists, capacity exceeded, etc.)
- **500**: Internal server error

## Security
- All endpoints require authentication
- Users can only access queues for clinics in their MoH office
- Proper authorization checks are implemented

## WebSocket Integration
The system includes real-time notifications through WebSocket:
- Queue updates
- User completions
- Queue status changes

## Database Schema

### ClinicQueue Collection
```json
{
  "id": "string",
  "clinicId": "string",
  "mohOfficeId": "string",
  "status": "active|closed",
  "maxCapacity": "number",
  "avgAppointmentTime": "number",
  "completedAppointments": "number",
  "createdAt": "datetime",
  "updatedAt": "datetime",
  "closedAt": "datetime"
}
```

### QueueEntry Collection
```json
{
  "id": "string",
  "queueId": "string",
  "clinicId": "string",
  "name": "string",
  "email": "string",
  "queuePosition": "number",
  "status": "waiting|completed",
  "joinedAt": "datetime",
  "completedAt": "datetime",
  "estimatedWaitTime": "number"
}
```

## Usage Example

```javascript
// Start queue
await fetch('/api/v1/moh/clinics/clinic-123/queue/start', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    maxCapacity: 50,
    avgAppointmentTime: 15
  })
});

// Add users to queue
await fetch('/api/v1/moh/clinics/clinic-123/queue/add-users', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    users: [
      { name: "Sarah Johnson", email: "sarah@email.com" },
      { name: "Emily Davis", email: "emily@email.com" }
    ]
  })
});

// Complete current appointment
await fetch('/api/v1/moh/clinics/clinic-123/queue/complete', {
  method: 'POST'
});

// Get queue status
const response = await fetch('/api/v1/moh/clinics/clinic-123/queue');
const queueData = await response.json();
```

## Notes
- The system is designed to be simple and not complicated
- Mother registration is handled by a separate mobile app
- All queue operations are logged for monitoring
- The system maintains data consistency across concurrent operations
- Queue state is persisted and survives server restarts
