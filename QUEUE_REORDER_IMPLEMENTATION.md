# Queue Reorder Implementation Guide

## 📋 Overview

The CareBloom queue system supports **manual reordering** of patients in the waiting queue. This allows clinic staff to adjust patient priority based on urgency, special needs, or other factors.

---

## 🎯 How Queue Reordering Works

### **Endpoint**
```
PUT /api/v1/moh/clinics/{clinicId}/queue/reorder
```

### **Request Body**
```json
{
  "patientIds": [
    "patient-id-1",  // Current patient (in-progress) - must be first
    "patient-id-2",  // New position 2
    "patient-id-3",  // New position 3
    "patient-id-4"   // New position 4
  ]
}
```

### **Response**
```json
{
  "success": true,
  "message": "Queue reordered successfully",
  "updatedCount": 4
}
```

---

## 🔄 Reordering Process

### **Step 1: Validation**

The system performs several validation checks:

1. ✅ **Clinic exists** - Verifies the clinic ID is valid
2. ✅ **All patient IDs exist** - Ensures all provided IDs belong to patients in the queue
3. ✅ **Current patient first** - Enforces that the patient "in-progress" must remain at position 1
4. ✅ **Patients belong to clinic** - Validates all patients are in the specified clinic's queue

**Code Location:** `NewQueueService.java` - `reorderQueue()` method

```java
// Validate clinic exists
Optional<Clinic> clinicOpt = clinicRepository.findById(clinicId);
if (clinicOpt.isEmpty()) {
    throw new IllegalArgumentException("Clinic not found");
}

// Validate all patient IDs exist
for (String patientId : patientIds) {
    boolean found = allPatients.stream().anyMatch(p -> p.getId().equals(patientId));
    if (!found) {
        throw new IllegalArgumentException("Patient ID " + patientId + " not found in queue");
    }
}
```

### **Step 2: Current Patient Protection**

**Important Rule:** The patient currently being served (status = "in-progress") **cannot be moved**. They must always remain at position 1.

```java
// If there's a current patient, ensure they're first in the list
if (currentPatient != null && !patientIds.isEmpty()) {
    if (!patientIds.get(0).equals(currentPatient.getId())) {
        throw new IllegalArgumentException("Current patient (in-progress) must remain first in queue");
    }
}
```

**Why?** 
- The current patient is actively being treated
- Moving them would disrupt ongoing care
- Only waiting patients can be reordered

### **Step 3: Update Positions**

The system updates each patient's position based on the new order:

```java
// Update positions based on the new order
for (int i = 0; i < patientIds.size(); i++) {
    String patientId = patientIds.get(i);
    QueueUser patient = allPatients.stream()
        .filter(p -> p.getId().equals(patientId))
        .findFirst()
        .orElseThrow();
    
    patient.setPosition(i + 1);           // New position (1-based index)
    patient.setWaitTime(i * 15);          // Wait time = position × 15 minutes
    queueUserRepository.save(patient);     // Save to database
}
```

### **Step 4: Recalculate Wait Times**

Wait times are automatically recalculated based on the new positions:

- **Formula:** `waitTime = (position - 1) × 15 minutes`
- **Example:**
  - Position 1 (in-progress): 0 minutes
  - Position 2: 15 minutes
  - Position 3: 30 minutes
  - Position 4: 45 minutes

---

## 🏗️ Architecture

### **Controller Layer**
**File:** `NewMoHQueueController.java`

```java
@PutMapping("/clinics/{clinicId}/queue/reorder")
public ResponseEntity<Map<String, Object>> reorderQueue(
    @PathVariable String clinicId, 
    @RequestBody Map<String, Object> requestBody
) {
    List<String> patientIds = (List<String>) requestBody.get("patientIds");
    
    if (patientIds == null || patientIds.isEmpty()) {
        return ResponseEntity.badRequest().body(
            Map.of("success", false, "error", "Patient IDs are required")
        );
    }
    
    Map<String, Object> response = newQueueService.reorderQueue(clinicId, patientIds);
    return ResponseEntity.ok(response);
}
```

### **Service Layer**
**File:** `NewQueueService.java`

Handles:
- Validation logic
- Position updates
- Wait time recalculation
- Database persistence
- Transaction management (`@Transactional`)

### **Repository Layer**
**File:** `QueueUserRepository.java`

Provides database queries:
```java
List<QueueUser> findByClinicIdOrderByPosition(String clinicId);
```

### **Model**
**File:** `QueueUser.java`

```java
@Data
@Document(collection = "queue_users")
public class QueueUser {
    @Id
    private String id;
    private String name;
    private String email;
    private String motherId;
    private String clinicId;
    private int position;              // Queue position (1-based)
    private String status;             // waiting, in-progress, completed, no-show
    private String joinedTime;         // When patient joined queue
    private String estimatedTime;      // Estimated appointment time
    private int waitTime;              // Wait time in minutes
    private String notes;
}
```

---

## 📝 Usage Examples

### **Example 1: Simple Reorder (No Current Patient)**

**Initial Queue:**
```
Position 1: Alice (waiting)
Position 2: Bob (waiting)
Position 3: Carol (waiting)
```

**Reorder Request:** Move Carol to front
```json
{
  "patientIds": ["carol-id", "alice-id", "bob-id"]
}
```

**Result:**
```
Position 1: Carol (waiting) - 0 min wait
Position 2: Alice (waiting) - 15 min wait
Position 3: Bob (waiting) - 30 min wait
```

### **Example 2: Reorder with Current Patient**

**Initial Queue:**
```
Position 1: Alice (in-progress) - Current patient
Position 2: Bob (waiting)
Position 3: Carol (waiting)
Position 4: David (waiting)
```

**Reorder Request:** Move David ahead of Bob
```json
{
  "patientIds": ["alice-id", "david-id", "bob-id", "carol-id"]
}
```

**Result:**
```
Position 1: Alice (in-progress) - 0 min wait
Position 2: David (waiting) - 15 min wait
Position 3: Bob (waiting) - 30 min wait
Position 4: Carol (waiting) - 45 min wait
```

### **Example 3: Invalid Reorder (Error Case)**

**Attempt:** Move current patient to position 3 ❌
```json
{
  "patientIds": ["bob-id", "carol-id", "alice-id", "david-id"]
}
```

**Error Response:**
```json
{
  "success": false,
  "error": "Current patient (in-progress) must remain first in queue"
}
```

---

## ⚠️ Important Rules & Constraints

### **1. Current Patient Always First**
- Patient with status "in-progress" **must** be at position 1
- Cannot reorder if current patient is not first in the list
- This protects ongoing appointments

### **2. Only Waiting & In-Progress Patients**
- Reorder only affects active queue members
- Completed patients are not included
- No-show patients are excluded

### **3. All or Nothing**
- You must provide IDs for **all active patients**
- Cannot partially reorder (missing patients will lose positions)
- Transaction ensures atomicity

### **4. Position Recalculation**
- Positions are 1-based (start at 1, not 0)
- Gaps are not allowed
- Sequential numbering enforced

### **5. Wait Time Auto-Update**
- Wait times recalculated automatically
- Based on 15-minute average appointment duration
- Can be customized if needed

---

## 🚨 Error Handling

### **Common Errors**

| Error | HTTP Status | Cause | Solution |
|-------|-------------|-------|----------|
| "Clinic not found" | 400 Bad Request | Invalid clinic ID | Verify clinic exists |
| "Patient IDs are required" | 400 Bad Request | Empty patientIds array | Include patient IDs |
| "Patient ID X not found in queue" | 400 Bad Request | Invalid patient ID | Check patient is in queue |
| "Current patient must remain first" | 400 Bad Request | In-progress patient not first | Put current patient at position 1 |
| "Failed to reorder queue" | 500 Internal Server Error | Database/system error | Check logs, retry |

### **Error Response Format**
```json
{
  "success": false,
  "error": "Detailed error message"
}
```

---

## 🔧 Implementation Details

### **Database Operations**

1. **Fetch current queue state** (1 query)
   ```sql
   db.queue_users.find({ clinicId: "clinic-123" }).sort({ position: 1 })
   ```

2. **Update each patient** (N queries, where N = number of patients)
   ```sql
   db.queue_users.updateOne(
     { _id: "patient-id" },
     { $set: { position: 2, waitTime: 15 } }
   )
   ```

3. **Transaction management**
   - All updates happen within a single transaction
   - Rollback on any failure
   - Ensures data consistency

### **Performance Considerations**

- **Time Complexity:** O(n) where n = number of patients
- **Space Complexity:** O(n) for patient list storage
- **Database Queries:** 1 read + n writes
- **Transaction:** Ensures ACID properties

**Optimization Opportunities:**
- Batch updates instead of individual saves
- Use bulk write operations
- Add caching for frequently accessed queues

---

## 🧪 Testing Scenarios

### **Test Case 1: Normal Reorder**
```
Given: Queue with 4 waiting patients
When: Reorder request with valid patient IDs
Then: Positions updated successfully, wait times recalculated
```

### **Test Case 2: Current Patient Protection**
```
Given: Queue with 1 in-progress and 3 waiting patients
When: Reorder request with current patient NOT first
Then: Error thrown, no changes made
```

### **Test Case 3: Invalid Patient ID**
```
Given: Queue with 3 patients
When: Reorder request includes non-existent patient ID
Then: Error thrown, queue unchanged
```

### **Test Case 4: Empty Queue**
```
Given: Empty queue
When: Reorder request sent
Then: No error, returns success (0 updated)
```

### **Test Case 5: Missing Patient ID**
```
Given: Queue with 5 patients
When: Reorder request only includes 3 patient IDs
Then: 3 patients updated, 2 others remain at old positions (may cause gaps)
⚠️ NOTE: Should provide ALL patient IDs to avoid position gaps
```

---

## 📊 Frontend Integration

### **API Call Example (React/Axios)**

```typescript
const reorderQueue = async (clinicId: string, patientIds: string[]) => {
  try {
    const response = await axios.put(
      `/api/v1/moh/clinics/${clinicId}/queue/reorder`,
      { patientIds },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    
    if (response.data.success) {
      console.log('Queue reordered successfully');
      // Refresh queue display
      await refreshQueue();
    }
  } catch (error) {
    console.error('Reorder failed:', error.response?.data?.error);
    alert('Failed to reorder queue: ' + error.response?.data?.error);
  }
};
```

### **Drag-and-Drop Implementation**

1. Display queue with drag handles
2. User drags patient to new position
3. Capture new order of patient IDs
4. Call reorder API with updated list
5. Show loading state
6. Refresh queue on success

**Libraries:**
- `react-beautiful-dnd` (React)
- `dnd-kit` (Modern alternative)
- `Sortable.js` (Vanilla JS)

---

## 🔐 Security & Permissions

### **Authorization**
- Requires MOH (Medical Officer of Health) role
- JWT token validation
- Clinic ownership verification

### **Audit Logging**
```java
logger.info("Reordering queue for clinic {} with {} patients", 
            clinicId, patientIds.size());
logger.info("Successfully reordered queue for clinic {}", clinicId);
```

**Recommended Enhancements:**
- Log user who performed reorder
- Track before/after queue states
- Store reorder history for audit trail

---

## 🚀 Future Enhancements

### **Potential Features**

1. **Undo/Redo Reordering**
   - Store queue snapshots
   - Allow reverting to previous order

2. **Priority Flags**
   - Mark urgent patients with priority flag
   - Auto-bubble high-priority patients

3. **Bulk Operations**
   - Move multiple patients at once
   - Insert patient at specific position

4. **Reorder Constraints**
   - Prevent moving patients more than X positions
   - Require reason for large position changes

5. **Real-time Sync**
   - WebSocket updates on reorder
   - Show other users' reorder attempts

6. **Analytics**
   - Track reorder frequency
   - Analyze patterns for optimization

---

## 📚 Related Documentation

- [Queue API Documentation](./QUEUE_API_DOCUMENTATION.md)
- [Queue System Overview](../carebloom-web/QUEUE_SYSTEM_INTERVIEW_SCRIPT.md)
- [Technology Connections Guide](../carebloom-web/TECHNOLOGY_CONNECTIONS_GUIDE.md)

---

## 📞 Support

For questions or issues:
- Check logs: `server.log` in backend root
- Review validation errors in response
- Ensure all patient IDs are valid
- Verify clinic queue is active

---

**Last Updated:** October 17, 2025  
**Version:** 1.0  
**Status:** ✅ Production Ready
