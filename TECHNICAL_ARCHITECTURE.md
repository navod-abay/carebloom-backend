# CareBloom Queue Management - Technical Architecture

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Frontend (React/TypeScript)                │
├─────────────────────────────────────────────────────────────────┤
│  Queue Management UI │ Real-time Updates │ Patient Dashboard   │
└─────────────────────┬───────────────────┬─────────────────────┘
                      │                   │
                   REST API            Server-Sent Events
                      │                   │
┌─────────────────────▼───────────────────▼─────────────────────┐
│                Spring Boot Backend (Port 8082)               │
├─────────────────────────────────────────────────────────────────┤
│                    Controller Layer                            │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ ClinicQueue     │  │ MoHClinic       │  │ SSE             │ │
│  │ Controller      │  │ Controller      │  │ Endpoints       │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                     Service Layer                              │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ QueueService    │  │ MoHClinicService│  │ QueueSSEService │ │
│  │ • startQueue()  │  │ • addMothers()  │  │ • subscribe()   │ │
│  │ • processNext() │  │ • removeMother()│  │ • broadcast()   │ │
│  │ • updateStatus()│  └─────────────────┘  └─────────────────┘ │
│  │ • reorderQueue()│                                           │
│  └─────────────────┘                                           │
├─────────────────────────────────────────────────────────────────┤
│                   Repository Layer                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ QueueUser       │  │ Clinic          │  │ Mother          │ │
│  │ Repository      │  │ Repository      │  │ Repository      │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│                     Data Models                                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ QueueUser       │  │ Clinic          │  │ QueueSettings   │ │
│  │ • position      │  │ • addedMothers  │  │ • maxCapacity   │ │
│  │ • status        │  │ • queueStatus   │  │ • avgTime       │ │
│  │ • waitTime      │  │ • queueSettings │  │ • autoClose     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    MongoDB Atlas Cloud                         │
├─────────────────────────────────────────────────────────────────┤
│  Collections:                                                  │
│  • queue_users     (Auto-created on first use)                 │
│  • clinics         (Enhanced with queue fields)                │
│  • mothers         (Existing, integrated)                      │
│  • moh_offices     (Existing, for authorization)               │
└─────────────────────────────────────────────────────────────────┘
```

## Implementation Flow Diagram

```
Queue Initialization Flow:
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Frontend   │    │ Controller  │    │ QueueService│    │  Database   │
│             │    │             │    │             │    │             │
├─────────────┤    ├─────────────┤    ├─────────────┤    ├─────────────┤
│POST /start  │───▶│startQueue() │───▶│Validate     │───▶│Find Clinic  │
│             │    │             │    │Clinic       │    │             │
│             │    │             │    │             │◀───┤Return Data  │
│             │    │             │    ├─────────────┤    ├─────────────┤
│             │    │             │    │Create Queue │───▶│Save QueueUser│
│             │    │             │    │Users        │    │Documents    │
│             │    │             │    │             │◀───┤Confirm Save │
│             │    │             │    ├─────────────┤    ├─────────────┤
│             │    │             │◀───┤Return Queue │    │             │
│             │    │             │    │Status       │    │             │
│             │◀───┤JSON Response│    │             │    │             │
│             │    │             │    │             │    │             │
├─────────────┤    ├─────────────┤    ├─────────────┤    ├─────────────┤
│             │    │             │    │Broadcast SSE│    │             │
│             │    │             │    │Update       │    │             │
│SSE Update   │◀───┴─────────────┴────┤             │    │             │
│Received     │                       │             │    │             │
└─────────────┘                       └─────────────┘    └─────────────┘
```

## Data Flow and Relationships

```
Mother Management → Queue Creation → Real-time Updates

┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│ Mother Record   │         │ Clinic Setup    │         │ Queue Creation  │
├─────────────────┤         ├─────────────────┤         ├─────────────────┤
│ • id            │────────▶│ • addedMothers[]│────────▶│ • QueueUser     │
│ • name          │         │ • queueSettings │         │   documents     │
│ • email         │         │ • queueStatus   │         │ • positions     │
│ • mohOfficeId   │         │                 │         │ • wait times    │
└─────────────────┘         └─────────────────┘         └─────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        Real-time State Management                       │
├─────────────────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │
│  │ Queue State     │  │ SSE Broadcast   │  │ Frontend Update │         │
│  │ • Current       │─▶│ • All connected │─▶│ • Live queue    │         │
│  │   patient       │  │   clients       │  │   display       │         │
│  │ • Waiting list  │  │ • Real-time     │  │ • Position      │         │
│  │ • Statistics    │  │   sync          │  │   updates       │         │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Technology Stack Details

### Backend Technologies:
```
Application Framework:
├── Spring Boot 3.5.0
├── Spring Web (REST APIs)
├── Spring Data MongoDB
├── Spring Security (Firebase Integration)
└── Spring Boot Actuator (Health Monitoring)

Database Layer:
├── MongoDB Atlas (Cloud Database)
├── Spring Data Repositories
├── Automatic Collection Creation
└── Custom Query Methods

Real-time Features:
├── Server-Sent Events (SSE)
├── ConcurrentHashMap (Connection Management)
├── Spring Web SSE Support
└── Automatic Reconnection Handling

Development Tools:
├── Maven (Build Tool)
├── Lombok (Code Generation)
├── SLF4J + Logback (Logging)
└── Spring Boot DevTools
```

### Key Design Patterns Applied:
```
Architectural Patterns:
├── Repository Pattern (Data Access)
├── Service Layer Pattern (Business Logic)
├── Observer Pattern (SSE Updates)
├── Factory Pattern (Queue Creation)
└── Strategy Pattern (Queue Processing)

Code Organization:
├── Controller → Service → Repository
├── DTO Pattern (Data Transfer)
├── Exception Handling Hierarchy
└── Configuration Management
```

## Performance Considerations

### Database Optimization:
```javascript
// Recommended MongoDB Indexes
db.queue_users.createIndex({ "clinicId": 1, "position": 1 })
db.queue_users.createIndex({ "clinicId": 1, "status": 1 })
db.clinics.createIndex({ "mohOfficeId": 1, "date": 1 })
```

### Concurrency Management:
```java
@Transactional  // Ensures data consistency
public QueueStatusResponse processNextPatient(String clinicId) {
    // Thread-safe queue operations
    // Atomic position updates
    // Consistent state management
}
```

### Real-time Performance:
- **SSE Latency:** < 100ms typical update time
- **Connection Management:** Automatic cleanup of dead connections
- **Memory Efficiency:** ConcurrentHashMap for thread-safe operations
- **Scalability:** Horizontal scaling support with stateless design
