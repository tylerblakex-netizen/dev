#!/bin/bash

# Gemini GitHub Integration - Setup Script
# Automates the deployment and configuration process

set -e

echo "🤖 Gemini GitHub Integration Setup"
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check requirements
check_requirements() {
    print_status "Checking requirements..."
    
    # Check if gcloud is installed
    if ! command -v gcloud &> /dev/null; then
        print_error "Google Cloud CLI is not installed. Please install it first."
        echo "Visit: https://cloud.google.com/sdk/docs/install"
        exit 1
    fi
    
    # Check if GitHub CLI is installed
    if ! command -v gh &> /dev/null; then
        print_warning "GitHub CLI is not installed. Some features may not work."
        echo "Visit: https://cli.github.com/"
    fi
    
    # Check if Python is installed
    if ! command -v python3 &> /dev/null; then
        print_error "Python 3 is not installed. Please install Python 3.11 or later."
        exit 1
    fi
    
    print_success "Requirements check completed"
}

# Setup Google Cloud
setup_gcloud() {
    print_status "Setting up Google Cloud..."
    
    # Get project ID
    read -p "Enter your Google Cloud Project ID: " PROJECT_ID
    
    if [ -z "$PROJECT_ID" ]; then
        print_error "Project ID cannot be empty"
        exit 1
    fi
    
    # Set project
    gcloud config set project "$PROJECT_ID"
    
    # Enable required APIs
    print_status "Enabling required APIs..."
    gcloud services enable \
        cloudfunctions.googleapis.com \
        aiplatform.googleapis.com \
        logging.googleapis.com \
        secretmanager.googleapis.com \
        cloudbuild.googleapis.com
    
    print_success "Google Cloud setup completed"
}

# Setup GitHub
setup_github() {
    print_status "Setting up GitHub integration..."
    
    # Get GitHub token
    read -p "Enter your GitHub Personal Access Token: " GITHUB_TOKEN
    
    if [ -z "$GITHUB_TOKEN" ]; then
        print_error "GitHub token cannot be empty"
        exit 1
    fi
    
    # Store token in Secret Manager
    echo "$GITHUB_TOKEN" | gcloud secrets create github-token --data-file=-
    
    # Get webhook secret (optional)
    read -p "Enter webhook secret (optional, press Enter to skip): " WEBHOOK_SECRET
    
    if [ ! -z "$WEBHOOK_SECRET" ]; then
        echo "$WEBHOOK_SECRET" | gcloud secrets create github-webhook-secret --data-file=-
    fi
    
    print_success "GitHub setup completed"
}

# Deploy Cloud Function
deploy_function() {
    print_status "Deploying Cloud Function..."
    
    # Set default region
    REGION=${REGION:-"us-central1"}
    
    # Deploy function
    gcloud functions deploy gemini-github-webhook \
        --runtime python311 \
        --trigger-http \
        --allow-unauthenticated \
        --entry-point github_webhook_handler \
        --source . \
        --region "$REGION" \
        --memory 512MB \
        --timeout 540s \
        --set-env-vars PROJECT_ID="$PROJECT_ID",LOCATION="$REGION" \
        --set-secrets GITHUB_TOKEN=github-token:latest
    
    # Get function URL
    FUNCTION_URL=$(gcloud functions describe gemini-github-webhook --region="$REGION" --format="value(httpsTrigger.url)")
    
    print_success "Function deployed successfully!"
    print_status "Function URL: $FUNCTION_URL"
    
    # Save URL for later use
    echo "$FUNCTION_URL" > function_url.txt
}

# Setup webhook
setup_webhook() {
    print_status "Setting up GitHub webhook..."
    
    read -p "Enter repository name (owner/repo) to configure webhook: " REPO_NAME
    
    if [ -z "$REPO_NAME" ]; then
        print_warning "No repository specified. Skipping webhook setup."
        return
    fi
    
    # Read function URL
    if [ -f "function_url.txt" ]; then
        FUNCTION_URL=$(cat function_url.txt)
    else
        read -p "Enter Cloud Function URL: " FUNCTION_URL
    fi
    
    # Create webhook using GitHub API
    curl -X POST \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/$REPO_NAME/hooks" \
        -d "{
            \"name\": \"web\",
            \"active\": true,
            \"events\": [\"pull_request\", \"issues\", \"push\", \"release\"],
            \"config\": {
                \"url\": \"$FUNCTION_URL\",
                \"content_type\": \"json\"
            }
        }"
    
    print_success "Webhook configured for $REPO_NAME"
}

# Create environment file
create_env_file() {
    print_status "Creating environment configuration..."
    
    cat > .env << EOF
# Gemini GitHub Integration Configuration
PROJECT_ID=$PROJECT_ID
GITHUB_TOKEN=$GITHUB_TOKEN
LOCATION=${REGION:-us-central1}
LOG_LEVEL=INFO

# Optional: Add webhook secret if configured
# GITHUB_WEBHOOK_SECRET=your_webhook_secret
EOF
    
    print_success "Environment file created (.env)"
}

# Test deployment
test_deployment() {
    print_status "Testing deployment..."
    
    if [ -f "function_url.txt" ]; then
        FUNCTION_URL=$(cat function_url.txt)
        
        # Test health endpoint
        HEALTH_RESPONSE=$(curl -s "${FUNCTION_URL}/health" || echo "failed")
        
        if [[ $HEALTH_RESPONSE == *"healthy"* ]]; then
            print_success "Deployment test passed!"
        else
            print_warning "Deployment test failed. Check function logs."
        fi
    else
        print_warning "Function URL not found. Cannot test deployment."
    fi
}

# Print contact information
print_contacts() {
    print_status "Support and Contact Information"
    echo ""
    echo "📞 Google Gemini Support:"
    echo "   - Primary: Google Cloud Support (cloud-support@google.com)"
    echo "   - Documentation: https://ai.google.dev/gemini-api/docs"
    echo "   - Community: https://ai.google.dev/community"
    echo ""
    echo "📞 GitHub Integration Support:"
    echo "   - Primary: GitHub Support (support@github.com)"
    echo "   - Developer Program: developer@github.com"
    echo "   - API Documentation: https://docs.github.com/en/rest"
    echo ""
    echo "📞 Technical Escalation:"
    echo "   - Google Cloud Technical: cloud-support@google.com"
    echo "   - GitHub Technical: premium-support@github.com (Enterprise)"
    echo "   - Security Issues: security@github.com, cloud-security@google.com"
    echo ""
}

# Main setup flow
main() {
    echo ""
    print_status "Starting Gemini GitHub Integration setup..."
    echo ""
    
    check_requirements
    echo ""
    
    setup_gcloud
    echo ""
    
    setup_github
    echo ""
    
    deploy_function
    echo ""
    
    setup_webhook
    echo ""
    
    create_env_file
    echo ""
    
    test_deployment
    echo ""
    
    print_contacts
    echo ""
    
    print_success "🎉 Setup completed successfully!"
    echo ""
    print_status "Next steps:"
    echo "1. Test the integration by creating a pull request"
    echo "2. Monitor logs: gcloud functions logs read gemini-github-webhook"
    echo "3. Check the README.md for usage instructions"
    echo ""
}

# Handle script arguments
case "${1:-setup}" in
    "setup")
        main
        ;;
    "contacts")
        print_contacts
        ;;
    "test")
        test_deployment
        ;;
    "help")
        echo "Usage: $0 [setup|contacts|test|help]"
        echo ""
        echo "Commands:"
        echo "  setup    - Run full setup process (default)"
        echo "  contacts - Display contact information"
        echo "  test     - Test current deployment"
        echo "  help     - Show this help message"
        ;;
    *)
        print_error "Unknown command: $1"
        echo "Use '$0 help' for usage information"
        exit 1
        ;;
esac
