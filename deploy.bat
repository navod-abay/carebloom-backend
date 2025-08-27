@echo off
REM CareBloom Backend - Google App Engine Deployment Script (Windows)

echo 🚀 CareBloom Backend - Google App Engine Deployment
echo ==================================================

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

echo ✅ Project: %PROJECT_ID%

REM Check if App Engine is enabled
gcloud app describe >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ⚠️  App Engine not initialized
    echo Initializing App Engine...
    gcloud app create --region=us-central1
)

echo 📦 Building application...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed
    pause
    exit /b 1
)

echo ✅ Build successful

echo 🚀 Deploying to App Engine...
call mvn appengine:deploy

if %ERRORLEVEL% EQU 0 (
    echo ✅ Deployment successful!
    echo.
    echo 🌐 Your application is available at:
    echo    https://%PROJECT_ID%.appspot.com
    echo.
    echo 📊 View logs:
    echo    gcloud app logs tail -s default
    echo.
    echo 📈 Monitor in Cloud Console:
    echo    https://console.cloud.google.com/appengine?project=%PROJECT_ID%
) else (
    echo ❌ Deployment failed
)

pause
