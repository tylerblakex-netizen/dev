# Gemini GitHub Integration Project

This project demonstrates how to integrate Google's Gemini AI with GitHub for various automation tasks.

## Features

1. **Code Review Assistant** - Automated code review using Gemini
2. **Issue Analysis** - AI-powered issue classification and response
3. **Documentation Generator** - Auto-generate docs from code
4. **Security Scanner** - AI-powered security vulnerability detection
5. **Release Notes Generator** - Intelligent release note creation

## Setup Instructions

### 1. Google Cloud Setup
- Create a Google Cloud project
- Enable Vertex AI API
- Create service account with Vertex AI permissions
- Download service account key

### 2. GitHub Setup
- Create GitHub App or Personal Access Token
- Set up webhook endpoints
- Configure repository permissions

### 3. Environment Variables
```bash
export GOOGLE_APPLICATION_CREDENTIALS="path/to/service-account.json"
export GITHUB_TOKEN="your_github_token"
export PROJECT_ID="your-gcp-project-id"
export LOCATION="us-central1"
```

## Architecture

```
GitHub Webhook → API Gateway → Cloud Function → Gemini API
                     ↓
               GitHub API Response
```

## Contact Information

### Google Gemini Support
- **Primary**: Google Cloud Support (cloud-support@google.com)
- **Developer Relations**: ai.google.dev/community
- **Vertex AI Sales**: Contact through Google Cloud Console

### GitHub Integration Support
- **GitHub Support**: support@github.com
- **Developer Program**: developer@github.com
- **Community**: GitHub Community Discussions

### Technical Documentation
- **Gemini API**: https://ai.google.dev/gemini-api/docs
- **GitHub REST API**: https://docs.github.com/en/rest
- **GitHub Webhooks**: https://docs.github.com/en/webhooks
- **Google Cloud Functions**: https://cloud.google.com/functions/docs
