@echo off
REM CareBloom Backend - CI/CD Setup Script for Google Cloud Platform (Windows)

echo 🔧 CareBloom Backend - CI/CD Setup
echo ==================================

REM Check if gcloud is installed
gcloud version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Google Cloud SDK is not installed
    echo Please install it from: https://cloud.google.com/sdk/docs/install
    pause
    exit /b 1
)

REM Check if authenticated
for /f "delims=" %%i in ('gcloud auth list --filter="status:ACTIVE" --format="value(account)" 2^>nul') do set ACCOUNT=%%i
if "%ACCOUNT%"=="" (
    echo ❌ Not authenticated with Google Cloud
    echo Please run: gcloud auth login
    pause
    exit /b 1
)

REM Get current project
for /f "delims=" %%i in ('gcloud config get-value project 2^>nul') do set PROJECT_ID=%%i
if "%PROJECT_ID%"=="" (
    echo ❌ No project set
    echo Please run: gcloud config set project YOUR_PROJECT_ID
    pause
    exit /b 1
)

echo ℹ️ Using project: %PROJECT_ID%
echo.

echo 🔐 Setting up Service Account for CI/CD...

REM Create service account
set SERVICE_ACCOUNT_NAME=github-actions-cicd
set SERVICE_ACCOUNT_EMAIL=%SERVICE_ACCOUNT_NAME%@%PROJECT_ID%.iam.gserviceaccount.com

echo Creating service account: %SERVICE_ACCOUNT_NAME%
gcloud iam service-accounts create %SERVICE_ACCOUNT_NAME% --description="Service account for GitHub Actions CI/CD" --display-name="GitHub Actions CI/CD" 2>nul

REM Grant necessary permissions
echo Granting permissions...
gcloud projects add-iam-policy-binding %PROJECT_ID% --member="serviceAccount:%SERVICE_ACCOUNT_EMAIL%" --role="roles/appengine.deployer" --quiet
gcloud projects add-iam-policy-binding %PROJECT_ID% --member="serviceAccount:%SERVICE_ACCOUNT_EMAIL%" --role="roles/storage.admin" --quiet
gcloud projects add-iam-policy-binding %PROJECT_ID% --member="serviceAccount:%SERVICE_ACCOUNT_EMAIL%" --role="roles/cloudbuild.builds.editor" --quiet
gcloud projects add-iam-policy-binding %PROJECT_ID% --member="serviceAccount:%SERVICE_ACCOUNT_EMAIL%" --role="roles/appengine.appAdmin" --quiet

echo ✅ Service account created and permissions granted

REM Create and download service account key
set KEY_FILE=github-actions-key.json
echo Creating service account key...
gcloud iam service-accounts keys create %KEY_FILE% --iam-account=%SERVICE_ACCOUNT_EMAIL%

if not exist "%KEY_FILE%" (
    echo ❌ Failed to create service account key
    pause
    exit /b 1
)

echo ✅ Service account key created: %KEY_FILE%
echo.

echo 📋 GitHub Secrets Configuration
echo ===============================
echo.
echo ℹ️ Add these secrets to your GitHub repository:
echo Go to: GitHub Repository → Settings → Secrets and variables → Actions
echo.
echo Secret Name: GCP_PROJECT_ID
echo Secret Value: %PROJECT_ID%
echo.
echo Secret Name: GCP_SA_KEY
echo Secret Value: (paste the entire content of %KEY_FILE%)
echo.
echo Secret Name: GOOGLE_MAPS_API_KEY
echo Secret Value: (your Google Maps API key)
echo.
echo Secret Name: MONGODB_URI (optional)
echo Secret Value: (your MongoDB connection string for production)
echo.

echo ⚠️ IMPORTANT: Keep the %KEY_FILE% file secure and do not commit it to Git!
echo.

echo 🔒 Security Recommendations
echo ============================
echo 1. Add %KEY_FILE% to .gitignore
echo 2. Store the key content in GitHub Secrets
echo 3. Delete the local key file after copying to GitHub
echo 4. Rotate the key regularly (every 90 days)
echo.

REM Add key file to gitignore if not already there
findstr /C:"%KEY_FILE%" .gitignore >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo %KEY_FILE% >> .gitignore
    echo ✅ Added %KEY_FILE% to .gitignore
)

echo.
echo 📝 Next Steps
echo =============
echo 1. Copy the content of %KEY_FILE% to GitHub Secrets (GCP_SA_KEY)
echo 2. Add your Google Maps API key to GitHub Secrets (GOOGLE_MAPS_API_KEY)
echo 3. Add your project ID to GitHub Secrets (GCP_PROJECT_ID): %PROJECT_ID%
echo 4. Push your code to the main branch to trigger deployment
echo 5. Monitor the deployment in GitHub Actions tab
echo.

echo ✅ CI/CD setup completed!
echo ℹ️ Your GitHub Actions workflow is ready to deploy to: https://%PROJECT_ID%.appspot.com
echo.

echo 🚀 Test the setup:
echo 1. Make a change to your code
echo 2. Commit and push to main branch
echo 3. Watch the magic happen in GitHub Actions!
echo.

echo 📄 Service Account Key Content (copy this to GitHub Secrets):
echo =============================================================
type %KEY_FILE%
echo.
echo =============================================================
echo.
echo ⚠️ Remember to delete this key file after copying to GitHub Secrets!
echo Run: del %KEY_FILE%
echo.
pause
