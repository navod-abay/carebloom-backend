#!/bin/bash

# CareBloom Backend - Google App Engine Deployment Script

echo "🚀 CareBloom Backend - Google App Engine Deployment"
echo "=================================================="

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ Google Cloud SDK is not installed"
    echo "Please install it from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Check if authenticated
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q "@"; then
    echo "❌ Not authenticated with Google Cloud"
    echo "Please run: gcloud auth login"
    exit 1
fi

# Get current project
PROJECT_ID=$(gcloud config get-value project)
if [ -z "$PROJECT_ID" ]; then
    echo "❌ No project set"
    echo "Please run: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi

echo "✅ Project: $PROJECT_ID"

# Check if App Engine is enabled
if ! gcloud app describe &> /dev/null; then
    echo "⚠️  App Engine not initialized"
    echo "Initializing App Engine..."
    gcloud app create --region=us-central1
fi

echo "📦 Building application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"

echo "🚀 Deploying to App Engine..."
mvn appengine:deploy

if [ $? -eq 0 ]; then
    echo "✅ Deployment successful!"
    echo ""
    echo "🌐 Your application is available at:"
    echo "   https://$PROJECT_ID.appspot.com"
    echo ""
    echo "📊 View logs:"
    echo "   gcloud app logs tail -s default"
    echo ""
    echo "📈 Monitor in Cloud Console:"
    echo "   https://console.cloud.google.com/appengine?project=$PROJECT_ID"
else
    echo "❌ Deployment failed"
    exit 1
fi
