# GitHub Copilot Integration Prompt for CareBloom Queue Frontend

## Context for Copilot:
I have implemented a comprehensive queue management backend for CareBloom clinic system using Spring Boot with the following features:

### Backend API Structure:
```typescript
// Base URL: http://localhost:8082/api/clinics/{clinicId}/queue

// Queue Management Endpoints:
POST /start                              // Initialize queue
GET /                                   // Get queue status  
POST /close?force=false                 // Close queue
POST /process-next                      // Move to next patient
POST /add                              // Add patient manually
PUT /settings                          // Update queue settings
PUT /patients/{patientId}/status       // Update patient status
DELETE /patients/{patientId}           // Remove patient
POST /reorder                          // Reorder queue
GET /events                            // Server-Sent Events subscription

// Mother Management:
POST /api/v1/moh/clinics/{id}/mothers     // Add mothers to clinic
DELETE /api/v1/moh/clinics/{id}/mothers/{motherId}  // Remove mother
```

### Expected Response Formats:
```typescript
// Queue Status Response
interface QueueStatusResponse {
  success: boolean;
  data: {
    clinicId: string;
    isActive: boolean;
    currentPatient?: {
      name: string;
      email: string;
      position: number;
      status: string;
      estimatedWaitTime?: number;
    };
    waitingQueue: Array<{
      name: string;
      email: string;
      position: number;
      status: string;
      estimatedWaitTime?: number;
    }>;
    stats: {
      totalPatients: number;
      completed: number;
      waiting: number;
      inProgress: number;
    };
  };
}

// Clinic with Queue Data
interface ClinicWithQueue {
  id: string;
  title: string;
  date: string;
  startTime: string;
  endTime?: string;
  doctorName: string;
  location: string;
  venue: string;
  addedMothers: Array<{
    id: string;
    name: string;
    email: string;
    phone?: string;
    dueDate: string;
    age: number;
  }>;
  queueSettings?: {
    isOpen: boolean;
    maxCapacity: number;
    avgAppointmentTime: number;
    closingTime: string;
    autoClose: boolean;
  };
  queueStatus?: 'open' | 'closed' | 'completed';
}
```

### Real-time Updates via Server-Sent Events:
```javascript
// SSE Connection Example
const eventSource = new EventSource(`/api/clinics/${clinicId}/queue/events`);
eventSource.onmessage = (event) => {
  const queueData = JSON.parse(event.data);
  // Update UI with real-time queue changes
};
```

## Frontend Integration Requirements:

### Please help me create a React TypeScript frontend integration that includes:

1. **Queue Management Service:**
   - API service class with all queue endpoints
   - TypeScript interfaces for all response types  
   - Error handling with proper HTTP status codes
   - Axios configuration with proper headers

2. **Real-time Queue Hook:**
   - Custom React hook for Server-Sent Events
   - Automatic reconnection on connection loss
   - Queue state management with real-time updates
   - Cleanup on component unmount

3. **Queue Management Components:**
   - QueueDashboard: Display current queue status with live updates
   - QueueControls: Start/stop queue, process next patient
   - PatientList: Show waiting queue with positions and estimated times
   - QueueSettings: Configure capacity, timing, and auto-close options

4. **Queue Operations Components:**
   - AddPatientModal: Manually add patients to queue
   - ReorderQueue: Drag-and-drop interface for queue reordering
   - PatientStatusControls: Update patient status (completed, no-show, etc.)
   - QueueStatistics: Real-time statistics and metrics display

5. **Mother Management Integration:**
   - MotherSelector: Add mothers to clinic for queue population
   - MotherList: Display and manage added mothers
   - Integration with existing mother management system

6. **State Management:**
   - Redux/Context setup for queue state
   - Actions for all queue operations
   - Reducers handling queue state updates
   - Selectors for queue data access

### Technical Requirements:
- **React 18+** with TypeScript
- **Axios** for HTTP requests
- **React Query/SWR** for data fetching and caching
- **Server-Sent Events** for real-time updates
- **Material-UI or Tailwind CSS** for styling
- **React Hook Form** for form management
- **React DnD** for drag-and-drop reordering

### Expected User Flows:

1. **Queue Initialization:**
   - Select clinic and date
   - Add mothers to clinic
   - Configure queue settings
   - Start queue with validation

2. **Queue Management:**
   - View current patient and waiting list
   - Process next patient
   - Update patient status
   - Handle no-shows

3. **Real-time Monitoring:**
   - Live queue updates without page refresh
   - Real-time position changes
   - Automatic wait time updates
   - Connection status indicators

4. **Queue Operations:**
   - Manually add walk-in patients
   - Reorder queue positions
   - Close queue with confirmation
   - View completion statistics

### Error Handling Requirements:
- Network connectivity issues
- Backend API errors
- SSE connection failures  
- Validation errors
- Concurrent operation conflicts

### Performance Requirements:
- Optimistic updates for queue operations
- Efficient re-rendering with real-time updates
- Proper loading states and skeleton screens
- Caching for frequently accessed data

## Sample Implementation Structure:

```typescript
// Expected folder structure
src/
  services/
    queueApi.ts          // API service
    queueSSE.ts          // SSE service
  hooks/
    useQueue.ts          // Queue state hook
    useQueueSSE.ts       // Real-time updates hook
  components/
    queue/
      QueueDashboard.tsx
      QueueControls.tsx  
      PatientList.tsx
      QueueSettings.tsx
  types/
    queue.types.ts       // TypeScript interfaces
  store/
    queueSlice.ts        // Redux slice
```

Please generate the complete frontend integration code with proper TypeScript types, error handling, real-time updates, and modern React patterns. Focus on creating a production-ready solution that matches the backend API structure I've implemented.
