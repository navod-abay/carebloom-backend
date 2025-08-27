# Quick Start: Deploy CareBloom to Google Cloud Platform

## 🎯 Why GCP?
Since you're already using Firebase and Google Maps APIs, deploying to GCP gives you:
- **Seamless integration** with existing Google services
- **Cost optimization** - Free tier + consolidated billing
- **Better performance** - Optimized network routing between Google services
- **Easy scaling** - Automatic scaling from 0 to many instances

## 🚀 Quick Setup (5 Steps)

### Step 1: Install Google Cloud SDK
```bash
# Windows (PowerShell as Administrator)
# Download and install from: https://cloud.google.com/sdk/docs/install-sdk

# Verify installation
gcloud version
```

### Step 2: Authenticate and Setup
```bash
# Login to Google Cloud
gcloud auth login

# Set your Firebase project as the GCP project
gcloud config set project YOUR_FIREBASE_PROJECT_ID

# Initialize App Engine (choose us-central1 for best performance)
gcloud app create --region=us-central1
```

### Step 3: Configure Environment Variables
Edit `app.yaml` and set your variables:
```yaml
env_variables:
  SPRING_PROFILES_ACTIVE: "prod"
  GOOGLE_MAPS_API_KEY: "your-actual-api-key"
  # Add any other environment variables you need
```

### Step 4: Build and Deploy
```bash
# Option A: Use the deployment script (Windows)
deploy.bat

# Option B: Manual deployment
mvn clean package -DskipTests
mvn appengine:deploy
```

### Step 5: Verify Deployment
Your app will be available at: `https://YOUR_PROJECT_ID.appspot.com`

Test the health endpoint: `https://YOUR_PROJECT_ID.appspot.com/health`

## 📊 Expected Costs

### Free Tier Coverage:
- **App Engine**: 28 instance hours/day (enough for development/testing)
- **Google Maps API**: First 1,000 requests free per month
- **Firebase**: Generous free tier for authentication

### Beyond Free Tier:
- **App Engine**: ~$0.05/hour per instance
- **Google Maps API**: $5 per 1,000 Distance Matrix requests
- **Total estimated**: $15-40/month for moderate usage

## 🔧 Configuration Files Added

1. **`app.yaml`** - App Engine configuration
2. **`application-prod.properties`** - Production settings
3. **`HealthController.java`** - Health check endpoint
4. **`deploy.bat`** - Windows deployment script
5. **Enhanced `pom.xml`** - App Engine Maven plugin

## 🚦 Post-Deployment

### Monitor Your App:
- **Logs**: `gcloud app logs tail -s default`
- **Console**: https://console.cloud.google.com/appengine
- **Health**: https://YOUR_PROJECT_ID.appspot.com/health

### Update CORS for Frontend:
In `application-prod.properties`, update:
```properties
app.cors.admin-origin=https://your-actual-admin-domain.com
app.cors.mother-origin=https://your-actual-mother-app.com
# ... etc
```

## 🎯 Benefits You'll Get

✅ **Auto-scaling**: Scales from 0 to handle any load automatically
✅ **Zero downtime**: Rolling deployments with health checks
✅ **Global CDN**: Built-in content delivery network
✅ **SSL/HTTPS**: Automatic SSL certificates
✅ **Monitoring**: Built-in metrics and logging
✅ **Integration**: Seamless with Firebase and Google Maps
✅ **Cost-effective**: Pay only for what you use

## 🆘 Quick Troubleshooting

**Build fails?**
```bash
mvn clean compile  # Check for compilation errors
```

**Deployment fails?**
```bash
gcloud app logs tail -s default  # Check deployment logs
```

**Can't access?**
- Check if App Engine is initialized: `gcloud app describe`
- Verify project is set: `gcloud config get-value project`

## 🚀 Ready to Deploy?

You now have everything configured! Your CareBloom backend with Google Maps integration is ready for cloud deployment.

Run: `deploy.bat` and your app will be live on Google Cloud! 🎉
