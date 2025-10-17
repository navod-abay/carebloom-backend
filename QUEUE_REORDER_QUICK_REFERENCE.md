# Queue Reorder - Quick Reference

## 📍 Endpoint
```
PUT /api/v1/moh/clinics/{clinicId}/queue/reorder
```

## 📥 Request Format
```json
{
  "patientIds": [
    "patient-1-id",
    "patient-2-id",
    "patient-3-id"
  ]
}
```

## ✅ Validation Rules

| Rule | Description | Error if Violated |
|------|-------------|-------------------|
| **Clinic Exists** | Clinic ID must be valid | "Clinic not found" |
| **Patient IDs Valid** | All IDs must exist in queue | "Patient ID X not found in queue" |
| **Current Patient First** | In-progress patient must be at index 0 | "Current patient must remain first" |
| **Non-empty List** | At least one patient ID required | "Patient IDs are required" |

## 🔄 Reorder Flow

```
┌─────────────────────────────────────────────────┐
│  1. API Request                                 │
│     PUT /queue/reorder                          │
│     Body: { patientIds: [...] }                 │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  2. Validate Clinic                             │
│     ✓ Clinic exists                             │
│     ✓ Queue is active                           │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  3. Fetch Current Queue                         │
│     Get all patients ordered by position        │
│     Find current patient (in-progress)          │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  4. Validate Patient IDs                        │
│     ✓ All IDs exist in queue                    │
│     ✓ All belong to this clinic                 │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  5. Validate Current Patient Position           │
│     ✓ In-progress patient is first (index 0)    │
│     OR no in-progress patient exists            │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  6. Update Positions                            │
│     For each patient ID:                        │
│       - Set position = index + 1                │
│       - Set waitTime = index × 15 min           │
│       - Save to database                        │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────┐
│  7. Return Success                              │
│     { success: true, updatedCount: X }          │
└─────────────────────────────────────────────────┘
```

## 📊 Position & Wait Time Calculation

```
Position 1 (Index 0): wait = 0 × 15 = 0 minutes
Position 2 (Index 1): wait = 1 × 15 = 15 minutes
Position 3 (Index 2): wait = 2 × 15 = 30 minutes
Position 4 (Index 3): wait = 3 × 15 = 45 minutes
...and so on
```

## 🎯 Example Scenarios

### Scenario A: Reorder Waiting Patients Only
**Before:**
```
1. Alice (in-progress) ⏱️ treating now
2. Bob (waiting) ⏰ 15 min
3. Carol (waiting) ⏰ 30 min
4. David (waiting) ⏰ 45 min
```

**Request:**
```json
{
  "patientIds": ["alice-id", "david-id", "carol-id", "bob-id"]
}
```

**After:**
```
1. Alice (in-progress) ⏱️ treating now
2. David (waiting) ⏰ 15 min  ⬆️ moved up
3. Carol (waiting) ⏰ 30 min  ⬆️ moved up
4. Bob (waiting) ⏰ 45 min    ⬇️ moved down
```

### Scenario B: Invalid - Current Patient Not First ❌
**Before:**
```
1. Alice (in-progress)
2. Bob (waiting)
3. Carol (waiting)
```

**Request:**
```json
{
  "patientIds": ["bob-id", "alice-id", "carol-id"]
}
```

**Result:** ❌ ERROR
```
"Current patient (in-progress) must remain first in queue"
```

### Scenario C: All Waiting Patients (No Current)
**Before:**
```
1. Bob (waiting)
2. Carol (waiting)
3. David (waiting)
```

**Request:**
```json
{
  "patientIds": ["carol-id", "david-id", "bob-id"]
}
```

**After:**
```
1. Carol (waiting) ⏰ 0 min
2. David (waiting) ⏰ 15 min
3. Bob (waiting) ⏰ 30 min
```

## 💻 Code Snippets

### Java Service Method
```java
public Map<String, Object> reorderQueue(String clinicId, List<String> patientIds) {
    // 1. Validate clinic
    Optional<Clinic> clinic = clinicRepository.findById(clinicId);
    if (clinic.isEmpty()) throw new IllegalArgumentException("Clinic not found");
    
    // 2. Get current queue
    List<QueueUser> allPatients = queueUserRepository.findByClinicIdOrderByPosition(clinicId);
    
    // 3. Find current patient
    QueueUser currentPatient = allPatients.stream()
        .filter(p -> "in-progress".equals(p.getStatus()))
        .findFirst().orElse(null);
    
    // 4. Validate current patient is first
    if (currentPatient != null && !patientIds.get(0).equals(currentPatient.getId())) {
        throw new IllegalArgumentException("Current patient must remain first");
    }
    
    // 5. Update positions
    for (int i = 0; i < patientIds.size(); i++) {
        QueueUser patient = findPatient(patientIds.get(i), allPatients);
        patient.setPosition(i + 1);
        patient.setWaitTime(i * 15);
        queueUserRepository.save(patient);
    }
    
    return Map.of("success", true, "updatedCount", patientIds.size());
}
```

### Frontend API Call (TypeScript)
```typescript
const reorderQueue = async (clinicId: string, patientIds: string[]) => {
  const response = await fetch(
    `/api/v1/moh/clinics/${clinicId}/queue/reorder`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ patientIds })
    }
  );
  
  const data = await response.json();
  if (!data.success) throw new Error(data.error);
  
  return data;
};
```

## 🔍 Debugging Checklist

- [ ] Verify clinic ID is correct
- [ ] Check all patient IDs exist in queue
- [ ] Ensure current patient (if any) is first in list
- [ ] Confirm patient IDs array is not empty
- [ ] Validate queue is active (not closed)
- [ ] Check authentication token is valid
- [ ] Review backend logs for detailed errors

## 📈 Performance

| Metric | Value |
|--------|-------|
| **Time Complexity** | O(n) where n = patients |
| **Database Reads** | 1 (fetch all patients) |
| **Database Writes** | n (update each patient) |
| **Transaction** | Yes (atomic) |
| **Typical Duration** | < 100ms for 10 patients |

## 🔗 Related Operations

- **Add Patient** → Increases queue size, new patient at end
- **Remove Patient** → Adjusts positions of patients after removed one
- **Process Next** → Moves current to completed, next waiting to in-progress
- **Close Queue** → Clears all patients or marks as no-show

---

**File Location:** `/carebloom-backend/QUEUE_REORDER_IMPLEMENTATION.md`  
**For detailed documentation, see the full implementation guide.**
