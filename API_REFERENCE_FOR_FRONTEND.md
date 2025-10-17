# CareBloom Queue API Reference for Frontend Integration

## Complete API Endpoint Documentation

### Queue Management Endpoints

#### 1. Initialize Queue
```http
POST /api/clinics/{clinicId}/queue/start
Content-Type: application/json

Response:
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
    "waitingQueue": [...],
    "stats": {
      "totalPatients": 10,
      "completed": 0,
      "waiting": 9,
      "inProgress": 1
    }
  }
}
```

#### 2. Get Queue Status
```http
GET /api/clinics/{clinicId}/queue
Content-Type: application/json

Response: Same as above
```

#### 3. Close Queue
```http
POST /api/clinics/{clinicId}/queue/close?force=false
Content-Type: application/json

Response (with confirmation needed):
{
  "requiresConfirmation": true,
  "waitingPatients": 5,
  "inProgressPatients": 1,
  "message": "There are still patients in the queue. Do you want to force close?"
}

Response (successful close):
{
  "success": true,
  "message": "Queue closed successfully",
  "finalStatus": { /* QueueStatusResponse */ }
}
```

#### 4. Process Next Patient
```http
POST /api/clinics/{clinicId}/queue/process-next
Content-Type: application/json

Response: QueueStatusResponse (updated queue state)
```

#### 5. Add Patient Manually  
```http
POST /api/clinics/{clinicId}/queue/add
Content-Type: application/json

Request Body:
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "motherId": "mother-456",
  "notes": "Emergency appointment"
}

Response: QueueStatusResponse (updated queue state)
```

#### 6. Update Queue Settings
```http
PUT /api/clinics/{clinicId}/queue/settings
Content-Type: application/json

Request Body:
{
  "isOpen": true,
  "maxCapacity": 50,
  "avgAppointmentTime": 20,
  "closingTime": "17:00", 
  "autoClose": true
}

Response:
{
  "success": true,
  "data": { /* Updated QueueSettings */ }
}
```

#### 7. Update Patient Status
```http
PUT /api/clinics/{clinicId}/queue/patients/{patientId}/status
Content-Type: application/json

Request Body:
{
  "status": "completed" // "waiting" | "in-progress" | "completed" | "no-show"
}

Response: QueueStatusResponse (updated queue state)
```

#### 8. Remove Patient
```http
DELETE /api/clinics/{clinicId}/queue/patients/{patientId}
Content-Type: application/json

Response: QueueStatusResponse (updated queue state with reordered positions)
```

#### 9. Reorder Queue
```http
POST /api/clinics/{clinicId}/queue/reorder
Content-Type: application/json

Request Body:
{
  "patientIds": ["patient-1", "patient-2", "patient-3"]
}

Response: QueueStatusResponse (updated queue state with new positions)
```

#### 10. Server-Sent Events
```http
GET /api/clinics/{clinicId}/queue/events
Accept: text/event-stream

SSE Events:
event: connected
data: "Connected to queue updates for clinic: clinic-123"

event: queue-update  
data: { /* QueueStatusResponse JSON */ }
```

### Mother Management Endpoints

#### Add Mothers to Clinic
```http
POST /api/v1/moh/clinics/{clinicId}/mothers
Content-Type: application/json
Authorization: Bearer {firebase-token}

Request Body:
["mother-id-1", "mother-id-2", "mother-id-3"]

Response:
{
  "success": true,
  "data": {
    "id": "clinic-123",
    "addedMothers": [
      {
        "id": "mother-1",
        "name": "Sarah Johnson", 
        "email": "sarah@example.com",
        "phone": "+94771234567",
        "dueDate": "2025-12-15",
        "age": 28
      }
    ]
  }
}
```

#### Remove Mother from Clinic
```http
DELETE /api/v1/moh/clinics/{clinicId}/mothers/{motherId}
Authorization: Bearer {firebase-token}

Response:
{
  "success": true,
  "data": { /* Updated clinic with removed mother */ }
}
```

## TypeScript Interface Definitions

```typescript
// Core Types
export interface QueueUser {
  id: string;
  name: string;
  email: string;
  motherId: string;
  clinicId: string;
  position: number;
  status: 'waiting' | 'in-progress' | 'completed' | 'no-show';
  joinedTime: string;
  estimatedTime?: string;
  waitTime: number;
  notes?: string;
}

export interface QueueSettings {
  isOpen: boolean;
  maxCapacity: number;
  avgAppointmentTime: number;
  closingTime: string;
  autoClose: boolean;
}

export interface AddedMother {
  id: string;
  name: string;
  email: string;
  phone?: string;
  dueDate: string;
  age: number;
}

export interface Clinic {
  id: string;
  title: string;
  description?: string;
  date: string;
  startTime: string;
  endTime?: string;
  doctorTitle?: string;
  doctorName: string;
  location: string;
  venue: string;
  maxCapacity?: number;
  notes?: string;
  unitIds: string[];
  registeredMotherIds: string[];
  addedMothers: AddedMother[];
  queueStatus?: 'open' | 'closed' | 'completed';
  queueSettings?: QueueSettings;
}

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}

export interface QueueStatusResponse {
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
}

export interface QueueCloseResponse {
  requiresConfirmation?: boolean;
  waitingPatients?: number;
  inProgressPatients?: number;
  message?: string;
  success?: boolean;
  finalStatus?: QueueStatusResponse;
}

// Request Types
export interface AddPatientRequest {
  name: string;
  email: string;
  motherId: string;
  notes?: string;
}

export interface UpdateStatusRequest {
  status: 'waiting' | 'in-progress' | 'completed' | 'no-show';
}

export interface ReorderQueueRequest {
  patientIds: string[];
}

export interface UpdateSettingsRequest extends QueueSettings {}
```

## Sample Frontend Service Implementation

```typescript
// services/queueApi.ts
import axios from 'axios';

export class QueueApiService {
  private baseUrl = 'http://localhost:8082/api/clinics';

  async startQueue(clinicId: string): Promise<ApiResponse<QueueStatusResponse>> {
    const response = await axios.post(`${this.baseUrl}/${clinicId}/queue/start`);
    return response.data;
  }

  async getQueueStatus(clinicId: string): Promise<ApiResponse<QueueStatusResponse>> {
    const response = await axios.get(`${this.baseUrl}/${clinicId}/queue`);
    return response.data;
  }

  async closeQueue(clinicId: string, force = false): Promise<QueueCloseResponse> {
    const response = await axios.post(
      `${this.baseUrl}/${clinicId}/queue/close?force=${force}`
    );
    return response.data;
  }

  async processNext(clinicId: string): Promise<ApiResponse<QueueStatusResponse>> {
    const response = await axios.post(`${this.baseUrl}/${clinicId}/queue/process-next`);
    return response.data;
  }

  async addPatient(
    clinicId: string, 
    patient: AddPatientRequest
  ): Promise<ApiResponse<QueueStatusResponse>> {
    const response = await axios.post(
      `${this.baseUrl}/${clinicId}/queue/add`,
      patient
    );
    return response.data;
  }

  async updatePatientStatus(
    clinicId: string,
    patientId: string,
    status: UpdateStatusRequest
  ): Promise<ApiResponse<QueueStatusResponse>> {
    const response = await axios.put(
      `${this.baseUrl}/${clinicId}/queue/patients/${patientId}/status`,
      status
    );
    return response.data;
  }

  async removePatient(
    clinicId: string,
    patientId: string
  ): Promise<ApiResponse<QueueStatusResponse>> {
    const response = await axios.delete(
      `${this.baseUrl}/${clinicId}/queue/patients/${patientId}`
    );
    return response.data;
  }

  async reorderQueue(
    clinicId: string,
    reorderData: ReorderQueueRequest
  ): Promise<ApiResponse<QueueStatusResponse>> {
    const response = await axios.post(
      `${this.baseUrl}/${clinicId}/queue/reorder`,
      reorderData
    );
    return response.data;
  }

  async updateSettings(
    clinicId: string,
    settings: UpdateSettingsRequest
  ): Promise<ApiResponse<QueueSettings>> {
    const response = await axios.put(
      `${this.baseUrl}/${clinicId}/queue/settings`,
      settings
    );
    return response.data;
  }

  // SSE Connection
  subscribeToQueueUpdates(
    clinicId: string,
    onUpdate: (data: QueueStatusResponse) => void,
    onError?: (error: Event) => void
  ): EventSource {
    const eventSource = new EventSource(
      `${this.baseUrl}/${clinicId}/queue/events`
    );

    eventSource.addEventListener('queue-update', (event) => {
      const data = JSON.parse(event.data);
      onUpdate(data);
    });

    if (onError) {
      eventSource.onerror = onError;
    }

    return eventSource;
  }
}
```

## Error Handling Examples

```typescript
// Common error responses from backend
interface ApiError {
  success: false;
  error: string;
}

// Error handling in service
try {
  const response = await queueApi.startQueue(clinicId);
  if (!response.success) {
    throw new Error(response.error);
  }
  return response.data;
} catch (error) {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    switch (status) {
      case 400:
        throw new Error('Invalid request data');
      case 404:
        throw new Error('Clinic not found');
      case 409:
        throw new Error('Queue is already active');
      case 500:
        throw new Error('Server error occurred');
      default:
        throw new Error('Network error occurred');
    }
  }
  throw error;
}
```

## Real-time Updates Hook Example

```typescript
// hooks/useQueueSSE.ts
export function useQueueSSE(clinicId: string) {
  const [queueData, setQueueData] = useState<QueueStatusResponse | null>(null);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!clinicId) return;

    const eventSource = queueApi.subscribeToQueueUpdates(
      clinicId,
      (data) => {
        setQueueData(data);
        setError(null);
      },
      (error) => {
        setError('Connection lost');
        setConnected(false);
      }
    );

    eventSource.onopen = () => setConnected(true);
    eventSource.onerror = () => setConnected(false);

    return () => {
      eventSource.close();
    };
  }, [clinicId]);

  return { queueData, connected, error };
}
```

This comprehensive API reference should provide Copilot with all the context needed to generate proper frontend integration code that matches your backend implementation exactly.
