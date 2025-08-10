"""
Configuration and setup utilities for Gemini GitHub Integration
"""

import os
import json
from typing import Dict, Optional
from dataclasses import dataclass


@dataclass
class Config:
    """Configuration settings for the application"""
    project_id: str
    github_token: str
    location: str = "us-central1"
    webhook_secret: Optional[str] = None
    log_level: str = "INFO"
    
    @classmethod
    def from_env(cls) -> 'Config':
        """Create configuration from environment variables"""
        return cls(
            project_id=os.getenv("PROJECT_ID", ""),
            github_token=os.getenv("GITHUB_TOKEN", ""),
            location=os.getenv("LOCATION", "us-central1"),
            webhook_secret=os.getenv("GITHUB_WEBHOOK_SECRET"),
            log_level=os.getenv("LOG_LEVEL", "INFO")
        )
    
    def validate(self) -> bool:
        """Validate required configuration"""
        return bool(self.project_id and self.github_token)


def setup_environment():
    """Setup environment for local development"""
    try:
        from dotenv import load_dotenv
        load_dotenv()
    except ImportError:
        pass  # dotenv not available in production


def get_supported_events() -> Dict[str, list]:
    """Get supported GitHub events and their actions"""
    return {
        "pull_request": ["opened", "synchronize", "reopened"],
        "issues": ["opened", "edited"],
        "push": ["refs/heads/main"],
        "release": ["published"],
        "issue_comment": ["created"]
    }


def get_file_extensions_for_analysis() -> list:
    """Get file extensions that should be analyzed"""
    return [
        '.py', '.js', '.ts', '.java', '.go', '.rs', '.cpp', '.c', '.h',
        '.php', '.rb', '.swift', '.kt', '.scala', '.cs', '.vb',
        '.html', '.css', '.scss', '.vue', '.jsx', '.tsx'
    ]


# Contact information for support and escalation
CONTACT_INFO = {
    "google_gemini_support": {
        "primary": "Google Cloud Support",
        "email": "cloud-support@google.com",
        "documentation": "https://ai.google.dev/gemini-api/docs",
        "community": "https://ai.google.dev/community",
        "billing": "Google Cloud Console → Billing",
        "vertex_ai_sales": "Contact through Google Cloud Console sales"
    },
    "github_integration_support": {
        "primary": "GitHub Support",
        "email": "support@github.com",
        "developer_program": "developer@github.com",
        "api_documentation": "https://docs.github.com/en/rest",
        "webhooks_documentation": "https://docs.github.com/en/webhooks",
        "community": "GitHub Community Discussions",
        "marketplace": "marketplace-support@github.com"
    },
    "technical_escalation": {
        "google_cloud_technical": "cloud-support@google.com",
        "github_technical": "premium-support@github.com (GitHub Enterprise)",
        "gemini_api_issues": "Through Google Cloud Console support tickets",
        "rate_limiting": "Both GitHub and Google APIs - check documentation",
        "quota_increases": "Google Cloud Console → IAM & Admin → Quotas"
    },
    "emergency_contacts": {
        "security_issues": {
            "github": "security@github.com",
            "google": "cloud-security@google.com"
        },
        "service_outages": {
            "google_status": "https://status.cloud.google.com/",
            "github_status": "https://www.githubstatus.com/"
        }
    }
}


def get_contact_info() -> Dict:
    """Get complete contact information"""
    return CONTACT_INFO


def format_contact_info() -> str:
    """Format contact information for display"""
    info = get_contact_info()
    
    formatted = "# Gemini GitHub Integration - Contact Information\n\n"
    
    for category, contacts in info.items():
        formatted += f"## {category.replace('_', ' ').title()}\n\n"
        
        if isinstance(contacts, dict):
            for key, value in contacts.items():
                if isinstance(value, dict):
                    formatted += f"### {key.replace('_', ' ').title()}\n"
                    for subkey, subvalue in value.items():
                        formatted += f"- **{subkey.replace('_', ' ').title()}**: {subvalue}\n"
                    formatted += "\n"
                else:
                    formatted += f"- **{key.replace('_', ' ').title()}**: {value}\n"
        
        formatted += "\n"
    
    return formatted


# Deployment configuration
DEPLOYMENT_CONFIG = {
    "cloud_function": {
        "runtime": "python311",
        "memory": "512MB",
        "timeout": "540s",
        "max_instances": 100,
        "environment_variables": [
            "PROJECT_ID",
            "LOCATION",
            "LOG_LEVEL"
        ],
        "secrets": [
            "GITHUB_TOKEN",
            "GITHUB_WEBHOOK_SECRET"
        ]
    },
    "required_apis": [
        "cloudfunctions.googleapis.com",
        "aiplatform.googleapis.com",
        "logging.googleapis.com",
        "secretmanager.googleapis.com"
    ],
    "iam_roles": [
        "roles/aiplatform.user",
        "roles/logging.logWriter",
        "roles/secretmanager.secretAccessor"
    ]
}


def get_deployment_config() -> Dict:
    """Get deployment configuration"""
    return DEPLOYMENT_CONFIG


if __name__ == "__main__":
    # Print contact information
    print(format_contact_info())
    
    # Print deployment requirements
    print("# Deployment Requirements\n")
    config = get_deployment_config()
    
    print("## Required Google Cloud APIs")
    for api in config["required_apis"]:
        print(f"- {api}")
    
    print("\n## Required IAM Roles")
    for role in config["iam_roles"]:
        print(f"- {role}")
    
    print("\n## Environment Variables")
    for var in config["cloud_function"]["environment_variables"]:
        print(f"- {var}")
    
    print("\n## Required Secrets")
    for secret in config["cloud_function"]["secrets"]:
        print(f"- {secret}")
