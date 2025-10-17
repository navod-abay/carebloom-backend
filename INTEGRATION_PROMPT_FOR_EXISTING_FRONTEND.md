# GitHub Copilot Integration Prompt - Update Existing Queue Frontend

## Context for Copilot:
I have an existing React TypeScript queue management frontend that needs to be updated to integrate with my new Spring Boot backend API. The backend is fully implemented and running at `http://localhost:8082`.

## Current Backend API Implementation:

### Base URL Structure:
```
http://localhost:8082/api/clinics/{clinicId}/queue
```

### Available Endpoints:
```typescript
// Queue Management Endpoints
POST   /api/clinics/{clinicId}/queue/start              // Initialize queue
GET    /api/clinics/{clinicId}/queue                    // Get current queue status
POST   /api/clinics/{clinicId}/queue/close?force=false  // Close queue (with confirmation)
POST   /api/clinics/{clinicId}/queue/process-next       // Move to next patient
POST   /api/clinics/{clinicId}/queue/add                // Add patient manually
PUT    /api/clinics/{clinicId}/queue/settings           // Update queue settings
PUT    /api/clinics/{clinicId}/queue/patients/{patientId}/status  // Update patient status
DELETE /api/clinics/{clinicId}/queue/patients/{patientId}         // Remove patient
POST   /api/clinics/{clinicId}/queue/reorder            // Reorder queue positions
GET    /api/clinics/{clinicId}/queue/events             // Server-Sent Events for real-time updates

// Mother Management for Clinics
POST   /api/v1/moh/clinics/{clinicId}/mothers           // Add mothers to clinic
DELETE /api/v1/moh/clinics/{clinicId}/mothers/{motherId} // Remove mother from clinic
```

### Backend Response Formats:
```typescript
// Standard API Response Wrapper
interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}

// Queue Status Response
interface QueueStatusResponse {
  clinicId: string;
  isActive: boolean;
  currentPatient?: {
    name: string;
    email: string;
    position: number;
    status: "waiting" | "in-progress" | "completed" | "no-show";
    estimatedWaitTime?: number;
  };
  waitingQueue: Array<{
    name: string;
    email: string;
    position: number;
    status: "waiting" | "in-progress" | "completed" | "no-show";
    estimatedWaitTime?: number;
  }>;
  stats: {
    totalPatients: number;
    completed: number;
    waiting: number;
    inProgress: number;
  };
}

// Queue Close Response (may require confirmation)
interface QueueCloseResponse {
  requiresConfirmation?: boolean;
  waitingPatients?: number;
  inProgressPatients?: number;
  message?: string;
  success?: boolean;
  finalStatus?: QueueStatusResponse;
}

// Request Body Types
interface AddPatientRequest {
  name: string;
  email: string;
  motherId: string;
  notes?: string;
}

interface UpdateStatusRequest {
  status: "waiting" | "in-progress" | "completed" | "no-show";
}

interface ReorderQueueRequest {
  patientIds: string[];
}

interface QueueSettings {
  isOpen: boolean;
  maxCapacity: number;
  avgAppointmentTime: number; // in minutes
  closingTime: string;        // HH:mm format
  autoClose: boolean;
}

// Mother Management
interface AddedMother {
  id: string;
  name: string;
  email: string;
  phone?: string;
  dueDate: string;
  age: number;
}
```

### Real-time Updates via Server-Sent Events:
```typescript
// SSE Connection for Real-time Queue Updates
const eventSource = new EventSource(`http://localhost:8082/api/clinics/${clinicId}/queue/events`);

eventSource.addEventListener('connected', (event) => {
  console.log('Connected to queue updates:', event.data);
});

eventSource.addEventListener('queue-update', (event) => {
  const queueData: QueueStatusResponse = JSON.parse(event.data);
  // Update your existing queue state with real-time data
});

eventSource.onerror = (error) => {
  console.error('SSE connection error:', error);
};
```

## Integration Requirements:

### Please help me update my existing frontend code to integrate with this backend:

1. **Update API Service Layer:**
   - Modify existing API calls to match new endpoint structure
   - Update request/response type interfaces to match backend exactly
   - Add new endpoints that may not exist in current frontend
   - Update base URL configuration to `http://localhost:8082`
   - Add proper error handling for each endpoint

2. **Update TypeScript Interfaces:**
   - Align existing interfaces with backend response formats
   - Add missing interfaces for new features (queue settings, SSE events)
   - Update existing types to match backend exactly
   - Ensure type safety across all queue operations

3. **Integrate Real-time Updates:**
   - Replace existing polling/refresh mechanisms with Server-Sent Events
   - Create or update SSE hook for real-time queue updates
   - Handle connection management (connect, disconnect, reconnect)
   - Update existing components to consume real-time data

4. **Update Queue State Management:**
   - Modify existing state structure to match backend response format
   - Update actions/reducers to handle new API response formats
   - Add new state properties for queue settings and statistics
   - Ensure proper state synchronization with real-time updates

5. **Component Integration Updates:**
   - Update existing queue components to use new API structure
   - Modify existing forms to send data in expected request format
   - Update existing status displays to match new response structure
   - Add missing functionality that exists in backend but not frontend

6. **Add Missing Features:**
   - Queue settings management (if not exists)
   - Patient manual addition (if not exists)  
   - Queue reordering functionality (if not exists)
   - Queue close confirmation flow (if not exists)
   - Mother management integration (if not exists)

### Specific Integration Tasks:

**For API Service Files:**
```typescript
// Update base URL and endpoint structure
const BASE_URL = 'http://localhost:8082';

// Example of expected API call structure
const startQueue = async (clinicId: string): Promise<ApiResponse<QueueStatusResponse>> => {
  const response = await axios.post(`${BASE_URL}/api/clinics/${clinicId}/queue/start`);
  return response.data;
};
```

**For Real-time Updates:**
```typescript
// Replace existing polling with SSE
export const useQueueRealtime = (clinicId: string) => {
  const [queueData, setQueueData] = useState<QueueStatusResponse | null>(null);
  const [connected, setConnected] = useState(false);
  
  useEffect(() => {
    if (!clinicId) return;
    
    const eventSource = new EventSource(`${BASE_URL}/api/clinics/${clinicId}/queue/events`);
    
    eventSource.addEventListener('queue-update', (event) => {
      const data: QueueStatusResponse = JSON.parse(event.data);
      setQueueData(data);
    });
    
    return () => eventSource.close();
  }, [clinicId]);
  
  return { queueData, connected };
};
```

**For State Management Updates:**
```typescript
// Update existing state structure to match backend
interface QueueState {
  currentQueue: QueueStatusResponse | null;
  settings: QueueSettings | null;
  loading: boolean;
  error: string | null;
  connected: boolean;
}
```

### Error Handling Requirements:
- Handle HTTP status codes: 400 (Bad Request), 404 (Not Found), 409 (Conflict), 500 (Server Error)
- Add network connectivity error handling
- Handle SSE connection failures with reconnection logic
- Add proper loading and error states for all operations

### Testing Integration:
- Ensure existing tests are updated for new API structure
- Add tests for new SSE functionality
- Update mock data to match backend response format
- Test error scenarios and edge cases

## Expected Outcome:
After integration, my existing queue frontend should:
- ✅ Connect successfully to Spring Boot backend at localhost:8082
- ✅ Send requests in the exact format expected by backend
- ✅ Receive and handle responses in backend format
- ✅ Display real-time queue updates via Server-Sent Events
- ✅ Handle all queue operations (start, process, add, remove, reorder, close)
- ✅ Manage queue settings and mother assignments
- ✅ Show proper error handling and loading states
- ✅ Maintain existing UI/UX while updating data integration

Please analyze my existing queue frontend code and provide the necessary updates, additions, and modifications to integrate with this Spring Boot backend API. Focus on maintaining my existing component structure while updating the data layer, API calls, and state management to work with the new backend implementation.

**Important:** Preserve my existing UI components and user experience - only update the data integration, API calls, and state management to match the backend API specification.
