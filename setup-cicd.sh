#!/bin/bash

# CareBloom Backend - CI/CD Setup Script for Google Cloud Platform

echo "🔧 CareBloom Backend - CI/CD Setup"
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️ $1${NC}"
}

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    print_error "Google Cloud SDK is not installed"
    echo "Please install it from: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Check if authenticated
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q "@"; then
    print_error "Not authenticated with Google Cloud"
    echo "Please run: gcloud auth login"
    exit 1
fi

# Get current project
PROJECT_ID=$(gcloud config get-value project)
if [ -z "$PROJECT_ID" ]; then
    print_error "No project set"
    echo "Please run: gcloud config set project YOUR_PROJECT_ID"
    exit 1
fi

print_info "Using project: $PROJECT_ID"

echo ""
echo "🔐 Setting up Service Account for CI/CD..."

# Create service account
SERVICE_ACCOUNT_NAME="github-actions-cicd"
SERVICE_ACCOUNT_EMAIL="$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com"

echo "Creating service account: $SERVICE_ACCOUNT_NAME"
gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME \
    --description="Service account for GitHub Actions CI/CD" \
    --display-name="GitHub Actions CI/CD" 2>/dev/null || print_warning "Service account may already exist"

# Grant necessary permissions
echo "Granting permissions..."
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$SERVICE_ACCOUNT_EMAIL" \
    --role="roles/appengine.deployer" --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$SERVICE_ACCOUNT_EMAIL" \
    --role="roles/storage.admin" --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$SERVICE_ACCOUNT_EMAIL" \
    --role="roles/cloudbuild.builds.editor" --quiet

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$SERVICE_ACCOUNT_EMAIL" \
    --role="roles/appengine.appAdmin" --quiet

print_status "Service account created and permissions granted"

# Create and download service account key
KEY_FILE="github-actions-key.json"
echo "Creating service account key..."
gcloud iam service-accounts keys create $KEY_FILE \
    --iam-account=$SERVICE_ACCOUNT_EMAIL

if [ -f "$KEY_FILE" ]; then
    print_status "Service account key created: $KEY_FILE"
else
    print_error "Failed to create service account key"
    exit 1
fi

echo ""
echo "📋 GitHub Secrets Configuration"
echo "==============================="
echo ""
print_info "Add these secrets to your GitHub repository:"
echo "Go to: GitHub Repository → Settings → Secrets and variables → Actions"
echo ""
echo "Secret Name: GCP_PROJECT_ID"
echo "Secret Value: $PROJECT_ID"
echo ""
echo "Secret Name: GCP_SA_KEY"
echo "Secret Value: (paste the entire content of $KEY_FILE)"
echo ""
echo "Secret Name: GOOGLE_MAPS_API_KEY"
echo "Secret Value: (your Google Maps API key)"
echo ""
echo "Secret Name: MONGODB_URI (optional)"
echo "Secret Value: (your MongoDB connection string for production)"
echo ""

print_warning "IMPORTANT: Keep the $KEY_FILE file secure and do not commit it to Git!"

echo ""
echo "🔒 Security Recommendations"
echo "==========================="
echo "1. Add $KEY_FILE to .gitignore"
echo "2. Store the key content in GitHub Secrets"
echo "3. Delete the local key file after copying to GitHub"
echo "4. Rotate the key regularly (every 90 days)"
echo ""

# Add key file to gitignore if not already there
if ! grep -q "$KEY_FILE" .gitignore 2>/dev/null; then
    echo "$KEY_FILE" >> .gitignore
    print_status "Added $KEY_FILE to .gitignore"
fi

echo ""
echo "📝 Next Steps"
echo "============="
echo "1. Copy the content of $KEY_FILE to GitHub Secrets (GCP_SA_KEY)"
echo "2. Add your Google Maps API key to GitHub Secrets (GOOGLE_MAPS_API_KEY)"
echo "3. Add your project ID to GitHub Secrets (GCP_PROJECT_ID): $PROJECT_ID"
echo "4. Push your code to the main branch to trigger deployment"
echo "5. Monitor the deployment in GitHub Actions tab"
echo ""

print_status "CI/CD setup completed!"
print_info "Your GitHub Actions workflow is ready to deploy to: https://$PROJECT_ID.appspot.com"

echo ""
echo "🚀 Test the setup:"
echo "1. Make a change to your code"
echo "2. Commit and push to main branch"
echo "3. Watch the magic happen in GitHub Actions!"
echo ""

# Display key file content for easy copying
echo "📄 Service Account Key Content (copy this to GitHub Secrets):"
echo "============================================================="
cat $KEY_FILE
echo ""
echo "============================================================="

print_warning "Remember to delete this key file after copying to GitHub Secrets!"
echo "Run: rm $KEY_FILE"
