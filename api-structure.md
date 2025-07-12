# API Structure Documentation - Hospitals UI

## Mothers Management Endpoints

### GET /api/v1/moh/mothers
**Description:** Fetch all mothers
**Method:** GET
**Base URL:** http://localhost:8082/api/v1/moh/
**Authentication:** Required (Bearer token)
**Request:** No parameters
**Response:**
```json
[
  {
    "id": "string",
    "name": "string",
    "age": "number",
    "bloodType": "string",
    "medicalHistory": "string",
    "allergies": "string",
    "currentMedications": "string",
    "emergencyContact": "string",
    "createdAt": "string",
    "updatedAt": "string"
  }
]
```

### GET /api/v1/moh/mothers/{id}
**Description:** Fetch a specific mother with complete details
**Method:** GET
**Authentication:** Required (Bearer token)
**Request:** motherId in path
**Response:**
```json
{
  "id": "string",
  "name": "string",
  "age": "number",
  "bloodType": "string",
  "medicalHistory": "string",
  "allergies": "string",
  "currentMedications": "string",
  "emergencyContact": "string",
  "childRecords": [
    {
      "id": "string",
      "motherId": "string",
      "name": "string",
      "dob": "string",
      "gender": "string",
      "birthWeight": "string",
      "birthLength": "string",
      "vaccinations": ["string"],
      "healthNotes": "string",
      "createdAt": "string",
      "updatedAt": "string"
    }
  ],
  "workshops": [
    {
      "id": "string",
      "name": "string",
      "date": "string",
      "description": "string"
    }
  ],
  "createdAt": "string",
  "updatedAt": "string"
}
```

### POST /api/v1/moh/mothers
**Description:** Create a new mother
**Method:** POST
**Authentication:** Required (Bearer token)
**Request:**
```json
{
  "name": "string",
  "age": "number",
  "bloodType": "string",
  "medicalHistory": "string",
  "allergies": "string",
  "currentMedications": "string",
  "emergencyContact": "string"
}
```
**Response:**
```json
{
  "id": "string",
  "name": "string",
  "age": "number",
  "bloodType": "string",
  "medicalHistory": "string",
  "allergies": "string",
  "currentMedications": "string",
  "emergencyContact": "string",
  "createdAt": "string",
  "updatedAt": "string"
}
```

### PUT /api/v1/moh/mothers/{id}
**Description:** Update a mother's information
**Method:** PUT
**Authentication:** Required (Bearer token)
**Request:** motherId in path
**Body:**
```json
{
  "name": "string",
  "age": "number",
  "bloodType": "string",
  "medicalHistory": "string",
  "allergies": "string",
  "currentMedications": "string",
  "emergencyContact": "string"
}
```
**Response:**
```json
{
  "id": "string",
  "name": "string",
  "age": "number",
  "bloodType": "string",
  "medicalHistory": "string",
  "allergies": "string",
  "currentMedications": "string",
  "emergencyContact": "string",
  "createdAt": "string",
  "updatedAt": "string"
}
```

### DELETE /api/v1/moh/mothers/{id}
**Description:** Delete a mother
**Method:** DELETE
**Authentication:** Required (Bearer token)
**Request:** motherId in path
**Response:** No content (204)

### GET /api/v1/moh/mothers/{motherId}/children
**Description:** Fetch child records for a specific mother
**Method:** GET
**Authentication:** Required (Bearer token)
**Request:** motherId in path
**Response:**
```json
[
  {
    "id": "string",
    "motherId": "string",
    "name": "string",
    "dob": "string",
    "gender": "string",
    "birthWeight": "string",
    "birthLength": "string",
    "vaccinations": ["string"],
    "healthNotes": "string",
    "createdAt": "string",
    "updatedAt": "string"
  }
]
```

### POST /api/v1/moh/mothers/{motherId}/children
**Description:** Create a new child record for a mother
**Method:** POST
**Authentication:** Required (Bearer token)
**Request:** motherId in path
**Body:**
```json
{
  "name": "string",
  "dob": "string",
  "gender": "string",
  "birthWeight": "string",
  "birthLength": "string",
  "healthNotes": "string"
}
```
**Response:**
```json
{
  "id": "string",
  "motherId": "string",
  "name": "string",
  "dob": "string",
  "gender": "string",
  "birthWeight": "string",
  "birthLength": "string",
  "vaccinations": [],
  "healthNotes": "string",
  "createdAt": "string",
  "updatedAt": "string"
}
```

### PUT /api/v1/moh/mothers/{motherId}/children/{childId}
**Description:** Update a child record
**Method:** PUT
**Authentication:** Required (Bearer token)
**Request:** motherId and childId in path
**Body:**
```json
{
  "name": "string",
  "dob": "string",
  "gender": "string",
  "birthWeight": "string",
  "birthLength": "string",
  "healthNotes": "string"
}
```
**Response:**
```json
{
  "id": "string",
  "motherId": "string",
  "name": "string",
  "dob": "string",
  "gender": "string",
  "birthWeight": "string",
  "birthLength": "string",
  "vaccinations": ["string"],
  "healthNotes": "string",
  "createdAt": "string",
  "updatedAt": "string"
}
```

### DELETE /api/v1/moh/mothers/{motherId}/children/{childId}
**Description:** Delete a child record
**Method:** DELETE
**Authentication:** Required (Bearer token)
**Request:** motherId and childId in path
**Response:** No content (204)

### GET /api/v1/moh/mothers/{motherId}/workshops
**Description:** Fetch workshops for a specific mother
**Method:** GET
**Authentication:** Required (Bearer token)
**Request:** motherId in path
**Response:**
```json
[
  {
    "id": "string",
    "name": "string",
    "date": "string",
    "description": "string"
  }
]
```

### GET /api/v1/moh/mothers/search?q={query}
**Description:** Search mothers by name or ID
**Method:** GET
**Authentication:** Required (Bearer token)
**Request:** search query as URL parameter
**Response:**
```json
[
  {
    "id": "string",
    "name": "string",
    "age": "number",
    "bloodType": "string",
    "medicalHistory": "string",
    "allergies": "string",
    "currentMedications": "string",
    "emergencyContact": "string",
    "createdAt": "string",
    "updatedAt": "string"
  }
]
```
