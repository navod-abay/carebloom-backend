# CareBloom Backend - Google Cloud Platform Deployment Guide

## Overview
This guide covers deploying the CareBloom Spring Boot backend to Google App Engine, taking advantage of the existing Firebase and Google Maps API integrations.

## Prerequisites
- Google Cloud Project (can use same project as Firebase)
- Google Cloud SDK installed
- Maven build working locally
- Firebase service account key

## Deployment Options Comparison

### 1. Google App Engine (Recommended)
**Best for**: Small to medium applications, easy deployment
- ✅ Zero infrastructure management
- ✅ Automatic scaling (0 to many instances)
- ✅ Free tier: 28 instance hours/day
- ✅ Native integration with Firebase/Google APIs
- ✅ Built-in SSL certificates
- ❌ Less control over runtime environment

### 2. Google Kubernetes Engine (GKE)
**Best for**: Microservices, high control requirements
- ✅ Full container orchestration
- ✅ Complete deployment control
- ✅ Better for complex architectures
- ❌ More complex setup
- ❌ Higher minimum cost

### 3. Compute Engine
**Best for**: Full VM control, predictable workloads
- ✅ Complete control over environment
- ✅ Cost-effective for sustained usage
- ❌ Manual infrastructure management
- ❌ No automatic scaling

## Step-by-Step App Engine Deployment

### Step 1: Project Setup
```bash
# Install Google Cloud SDK (if not installed)
# Windows: Download from https://cloud.google.com/sdk/docs/install
# Mac: brew install google-cloud-sdk
# Linux: Follow official guide

# Authenticate
gcloud auth login

# Set project (use your Firebase project ID)
gcloud config set project your-firebase-project-id

# Enable required APIs
gcloud services enable appengine.googleapis.com
gcloud services enable cloudbuild.googleapis.com
```

### Step 2: App Engine Configuration
Create `app.yaml` in project root:
```yaml
runtime: java21
instance_class: F2

automatic_scaling:
  min_instances: 0
  max_instances: 10
  target_cpu_utilization: 0.6

env_variables:
  SPRING_PROFILES_ACTIVE: "prod"
  GOOGLE_MAPS_API_KEY: "your-api-key-here"

resources:
  cpu: 1
  memory_gb: 2
```

### Step 3: Production Configuration
Create `application-prod.properties`:
```properties
# Production MongoDB URI (consider MongoDB Atlas)
spring.data.mongodb.uri=${MONGODB_URI:mongodb+srv://user:pass@cluster.mongodb.net/carebloom}

# Google Maps API
google.maps.api.key=${GOOGLE_MAPS_API_KEY}

# Logging for production
logging.level.root=INFO
logging.level.com.example.carebloom=DEBUG

# CORS for production domains
app.cors.admin-origin=https://your-admin-domain.com
app.cors.mother-origin=https://your-mother-app.com
app.cors.midwife-origin=https://your-midwife-app.com
app.cors.vendor-origin=https://your-vendor-app.com
app.cors.moh-origin=https://your-moh-app.com

# Server configuration
server.port=8080
```

### Step 4: Maven Plugin Configuration
Add to `pom.xml`:
```xml
<plugins>
  <!-- Existing plugins -->
  
  <!-- Google App Engine Plugin -->
  <plugin>
    <groupId>com.google.cloud.tools</groupId>
    <artifactId>appengine-maven-plugin</artifactId>
    <version>2.4.4</version>
    <configuration>
      <version>1</version>
      <projectId>your-firebase-project-id</projectId>
    </configuration>
  </plugin>
</plugins>
```

### Step 5: Firebase Service Account
```bash
# Place firebase-service-account.json in src/main/resources
# Ensure it's in .gitignore for security
echo "src/main/resources/firebase-service-account.json" >> .gitignore
```

### Step 6: Build and Deploy
```bash
# Clean and package
mvn clean package -DskipTests

# Deploy to App Engine
mvn appengine:deploy

# View logs
gcloud app logs tail -s default
```

## Cost Estimation

### App Engine Standard (Recommended)
- **Free Tier**: 28 instance hours/day (enough for development/testing)
- **Beyond Free Tier**: ~$0.05-0.10 per hour per instance
- **Estimated Monthly Cost**: $10-50 for small to medium usage

### Additional Google Services
- **Google Maps API**: $5 per 1,000 requests (Distance Matrix)
- **Firebase**: Pay-as-you-use (likely minimal for your app)
- **MongoDB Atlas**: $9/month for basic cluster (recommended over self-hosted)

### Total Estimated Monthly Cost: $25-75

## Security Considerations

### 1. Environment Variables
```yaml
# In app.yaml
env_variables:
  MONGODB_URI: "secure-connection-string"
  GOOGLE_MAPS_API_KEY: "restricted-api-key"
  JWT_SECRET: "secure-random-string"
```

### 2. API Key Restrictions
- Restrict Google Maps API key to your App Engine domain
- Enable only required APIs (Distance Matrix, Geocoding)
- Set daily quotas to prevent unexpected charges

### 3. CORS Configuration
- Update CORS origins to your production domains
- Remove localhost origins in production

## Monitoring and Maintenance

### 1. Cloud Monitoring
```bash
# Enable monitoring
gcloud services enable monitoring.googleapis.com

# View metrics in Cloud Console
# Go to: Monitoring > Metrics Explorer
```

### 2. Logging
```bash
# View application logs
gcloud app logs tail -s default

# Filter logs
gcloud app logs read --filter="severity>=ERROR"
```

### 3. Health Checks
Add health check endpoint:
```java
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }
}
```

## CI/CD Pipeline (Optional)

### GitHub Actions for Auto-Deployment
```yaml
# .github/workflows/deploy.yml
name: Deploy to App Engine

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 21
      uses: actions/setup-java@v2
      with:
        java-version: '21'
        
    - name: Set up Cloud SDK
      uses: google-github-actions/setup-gcloud@v0
      with:
        project_id: ${{ secrets.GCP_PROJECT_ID }}
        service_account_key: ${{ secrets.GCP_SA_KEY }}
        
    - name: Deploy to App Engine
      run: mvn appengine:deploy
```

## Alternative: Cloud Run (Containerized Deployment)

If you prefer containerized deployment:

### Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim

COPY target/carebloom-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Deploy to Cloud Run
```bash
# Build and deploy
gcloud run deploy carebloom-backend \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated
```

## Next Steps

1. **Choose deployment option** (App Engine recommended)
2. **Set up production database** (MongoDB Atlas)
3. **Configure environment variables**
4. **Deploy and test**
5. **Set up monitoring**
6. **Configure CI/CD** (optional)

## Benefits Summary

✅ **Cost-effective**: Free tier + pay-as-you-use
✅ **Scalable**: Automatic scaling from 0 to many instances
✅ **Integrated**: Native Firebase and Google APIs integration
✅ **Secure**: Enterprise-grade security
✅ **Global**: Deploy close to Sri Lankan users
✅ **Managed**: Zero infrastructure management
