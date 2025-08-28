# CI/CD Setup for CareBloom Backend on Google Cloud Platform

## 🎯 CI/CD Options Overview

| Option | Complexity | Cost | Features | Best For |
|--------|------------|------|----------|----------|
| **GitHub Actions** | Simple | Free | Basic CI/CD, easy setup | Most projects |
| **Google Cloud Build** | Medium | Pay-per-use | Native GCP integration | GCP-focused teams |
| **GitLab CI** | Medium | Free tier | Advanced features | GitLab users |
| **Jenkins** | Complex | Self-hosted | Full control | Enterprise |

## 🚀 Option 1: GitHub Actions (Recommended)

### Why GitHub Actions?
- ✅ **Free**: 2,000 minutes/month for private repos
- ✅ **Simple**: Easy YAML configuration
- ✅ **Integrated**: Works seamlessly with GitHub
- ✅ **Marketplace**: Thousands of pre-built actions

### Step 1: Create Service Account
```bash
# Create service account for deployment
gcloud iam service-accounts create github-actions \
    --description="Service account for GitHub Actions" \
    --display-name="GitHub Actions"

# Get your project ID
PROJECT_ID=$(gcloud config get-value project)

# Grant necessary permissions
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/appengine.deployer"

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/storage.admin"

gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:github-actions@$PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/cloudbuild.builds.editor"

# Create and download key
gcloud iam service-accounts keys create github-actions-key.json \
    --iam-account=github-actions@$PROJECT_ID.iam.gserviceaccount.com
```

### Step 2: Configure GitHub Secrets
Go to your GitHub repository → Settings → Secrets and variables → Actions

Add these secrets:
- `GCP_PROJECT_ID`: Your Google Cloud project ID
- `GCP_SA_KEY`: Content of `github-actions-key.json` file (entire JSON)
- `GOOGLE_MAPS_API_KEY`: Your Google Maps API key

### Step 3: Create GitHub Actions Workflow
Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Google App Engine

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Run tests
      run: mvn clean test
      
    - name: Generate test report
      uses: dorny/test-reporter@v1
      if: success() || failure()
      with:
        name: Maven Tests
        path: target/surefire-reports/*.xml
        reporter: java-junit

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main' && github.event_name == 'push'
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
        
    - name: Build application
      run: mvn clean package -DskipTests
      
    - name: Set up Google Cloud SDK
      uses: google-github-actions/setup-gcloud@v1
      with:
        project_id: ${{ secrets.GCP_PROJECT_ID }}
        service_account_key: ${{ secrets.GCP_SA_KEY }}
        export_default_credentials: true
        
    - name: Configure app.yaml with secrets
      run: |
        sed -i 's/your-actual-api-key/${{ secrets.GOOGLE_MAPS_API_KEY }}/g' app.yaml
        
    - name: Deploy to App Engine
      run: gcloud app deploy --quiet
      
    - name: Verify deployment
      run: |
        APP_URL="https://${{ secrets.GCP_PROJECT_ID }}.appspot.com"
        echo "Application deployed to: $APP_URL"
        curl -f "$APP_URL/health" || exit 1
        
    - name: Notify deployment success
      if: success()
      run: |
        echo "🎉 Deployment successful!"
        echo "Application URL: https://${{ secrets.GCP_PROJECT_ID }}.appspot.com"
```

### Step 4: Update app.yaml for CI/CD
```yaml
runtime: java21
instance_class: F2

automatic_scaling:
  min_instances: 0
  max_instances: 10
  target_cpu_utilization: 0.6

env_variables:
  SPRING_PROFILES_ACTIVE: "prod"
  GOOGLE_MAPS_API_KEY: "your-actual-api-key"  # Will be replaced by CI/CD

resources:
  cpu: 1
  memory_gb: 2

readiness_check:
  path: "/health"
  check_interval_sec: 5
  timeout_sec: 4
  failure_threshold: 2
  success_threshold: 2

liveness_check:
  path: "/health"
  check_interval_sec: 30
  timeout_sec: 4
  failure_threshold: 4
  success_threshold: 2
```

## 🔧 Option 2: Google Cloud Build

### Step 1: Create cloudbuild.yaml
```yaml
steps:
  # Test
  - name: 'maven:3.9.4-openjdk-21'
    entrypoint: 'mvn'
    args: ['clean', 'test']
    
  # Build
  - name: 'maven:3.9.4-openjdk-21'
    entrypoint: 'mvn'
    args: ['clean', 'package', '-DskipTests']
    
  # Deploy
  - name: 'gcr.io/google.com/cloudsdktool/cloud-sdk'
    entrypoint: 'gcloud'
    args: ['app', 'deploy', '--quiet']

substitutions:
  _GOOGLE_MAPS_API_KEY: '${_GOOGLE_MAPS_API_KEY}'

options:
  substitution_option: 'ALLOW_LOOSE'
  
timeout: '1200s'
```

### Step 2: Set up Cloud Build Trigger
```bash
# Enable Cloud Build API
gcloud services enable cloudbuild.googleapis.com

# Create build trigger
gcloud builds triggers create github \
    --repo-name=carebloom-backend \
    --repo-owner=navod-abay \
    --branch-pattern="^main$" \
    --build-config=cloudbuild.yaml
```

## 🚦 Option 3: GitLab CI/CD

### .gitlab-ci.yml
```yaml
stages:
  - test
  - build
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"

cache:
  paths:
    - .m2/repository/

test:
  stage: test
  image: maven:3.9.4-openjdk-21
  script:
    - mvn clean test
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml

build:
  stage: build
  image: maven:3.9.4-openjdk-21
  script:
    - mvn clean package -DskipTests
  artifacts:
    paths:
      - target/*.jar
    expire_in: 1 hour

deploy:
  stage: deploy
  image: google/cloud-sdk:alpine
  before_script:
    - echo $GCP_SERVICE_KEY | base64 -d > gcloud-service-key.json
    - gcloud auth activate-service-account --key-file gcloud-service-key.json
    - gcloud config set project $GCP_PROJECT_ID
  script:
    - gcloud app deploy --quiet
  only:
    - main
```

## 🔄 Advanced CI/CD Features

### 1. Multi-Environment Setup
```yaml
# .github/workflows/deploy.yml (Enhanced)
strategy:
  matrix:
    environment: [staging, production]
    
steps:
  - name: Deploy to ${{ matrix.environment }}
    run: |
      if [ "${{ matrix.environment }}" == "staging" ]; then
        gcloud app deploy --version=staging --no-promote
      else
        gcloud app deploy --promote
      fi
```

### 2. Database Migrations
```yaml
- name: Run database migrations
  run: |
    # Add your database migration commands here
    echo "Running database migrations..."
    # mvn flyway:migrate (if using Flyway)
```

### 3. Integration Tests
```yaml
- name: Run integration tests
  run: |
    mvn test -Dtest="*IntegrationTest"
  env:
    SPRING_PROFILES_ACTIVE: integration
```

### 4. Security Scanning
```yaml
- name: Run security scan
  uses: securecodewarrior/github-action-add-sarif@v1
  with:
    sarif-file: 'security-scan-results.sarif'
```

## 📊 Monitoring and Notifications

### 1. Slack Notifications
```yaml
- name: Notify Slack
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### 2. Email Notifications
```yaml
- name: Send email notification
  if: failure()
  uses: dawidd6/action-send-mail@v3
  with:
    server_address: smtp.gmail.com
    server_port: 587
    username: ${{ secrets.EMAIL_USERNAME }}
    password: ${{ secrets.EMAIL_PASSWORD }}
    subject: "CareBloom Backend Deployment Failed"
    body: "Deployment failed for commit ${{ github.sha }}"
    to: developer@carebloom.com
```

## 🔒 Security Best Practices

### 1. Secret Management
```bash
# Use Google Secret Manager instead of GitHub secrets for production
gcloud secrets create google-maps-api-key --data-file=api-key.txt
```

### 2. Branch Protection
- Require pull request reviews
- Require status checks to pass
- Require branches to be up to date
- Restrict pushes to main branch

### 3. Dependency Scanning
```yaml
- name: Run dependency check
  run: mvn org.owasp:dependency-check-maven:check
```

## 📈 Performance Optimization

### 1. Build Caching
```yaml
- name: Cache dependencies
  uses: actions/cache@v3
  with:
    path: |
      ~/.m2
      ~/.gradle
    key: ${{ runner.os }}-build-${{ hashFiles('**/*.xml', '**/*.gradle') }}
```

### 2. Parallel Jobs
```yaml
jobs:
  test:
    strategy:
      matrix:
        test-group: [unit, integration, e2e]
    steps:
      - run: mvn test -Dgroups=${{ matrix.test-group }}
```

## 🚀 Quick Start: GitHub Actions Setup

1. **Create service account and get key**
2. **Add secrets to GitHub repository**
3. **Create workflow file** (use the GitHub Actions example above)
4. **Push to main branch**
5. **Watch the magic happen!** ✨

Your CI/CD pipeline will:
- ✅ Run tests on every push/PR
- ✅ Deploy to App Engine on main branch
- ✅ Verify deployment health
- ✅ Cache dependencies for faster builds
- ✅ Provide detailed logs and reports

## 💡 Pro Tips

1. **Start simple**: Begin with basic GitHub Actions, add features gradually
2. **Test locally**: Use `act` to run GitHub Actions locally
3. **Monitor costs**: Track Cloud Build minutes and optimize accordingly
4. **Use staging**: Always test in staging before production
5. **Keep secrets secure**: Rotate keys regularly, use least privilege

This setup gives you production-ready CI/CD with automatic testing, building, and deployment! 🎉
