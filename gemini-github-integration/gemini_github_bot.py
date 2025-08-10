"""
Gemini GitHub Integration - Core API Client
Handles communication between GitHub webhooks and Google Gemini AI
"""

import os
import json
import logging
from typing import Dict, List, Optional, Any
from dataclasses import dataclass
from datetime import datetime

import vertexai
from vertexai.generative_models import GenerativeModel, Part
import requests
from github import Github


@dataclass
class GitHubEvent:
    """Represents a GitHub webhook event"""
    event_type: str
    repository: str
    action: str
    data: Dict[str, Any]
    timestamp: datetime = None
    
    def __post_init__(self):
        if self.timestamp is None:
            self.timestamp = datetime.utcnow()


class GeminiClient:
    """Google Gemini AI client for code analysis and generation"""
    
    def __init__(self, project_id: str, location: str = "us-central1"):
        self.project_id = project_id
        self.location = location
        
        # Initialize Vertex AI
        vertexai.init(project=project_id, location=location)
        self.model = GenerativeModel("gemini-1.5-pro-preview-0409")
        
        logging.info(f"Initialized Gemini client for project {project_id}")
    
    async def analyze_code_changes(self, diff_content: str, file_path: str) -> str:
        """Analyze code changes and provide review feedback"""
        prompt = f"""
        You are an expert code reviewer. Analyze the following code diff and provide:
        1. Code quality assessment
        2. Potential bugs or issues
        3. Security concerns
        4. Performance implications
        5. Best practice recommendations
        
        File: {file_path}
        
        Diff:
        {diff_content}
        
        Provide your analysis in markdown format with clear sections.
        """
        
        try:
            response = self.model.generate_content(prompt)
            return response.text
        except Exception as e:
            logging.error(f"Error analyzing code: {e}")
            return f"Error during analysis: {str(e)}"
    
    async def generate_documentation(self, code_content: str, file_path: str) -> str:
        """Generate documentation for code"""
        prompt = f"""
        Generate comprehensive documentation for the following code:
        
        File: {file_path}
        
        Code:
        {code_content}
        
        Include:
        1. Purpose and functionality
        2. Parameters and return values
        3. Usage examples
        4. Dependencies
        5. Notes and considerations
        
        Format as markdown documentation.
        """
        
        try:
            response = self.model.generate_content(prompt)
            return response.text
        except Exception as e:
            logging.error(f"Error generating documentation: {e}")
            return f"Error generating documentation: {str(e)}"
    
    async def analyze_security_issues(self, code_content: str) -> List[Dict[str, str]]:
        """Analyze code for security vulnerabilities"""
        prompt = f"""
        Analyze the following code for security vulnerabilities:
        
        {code_content}
        
        Return findings in JSON format with:
        - severity: (low/medium/high/critical)
        - description: Brief description of the issue
        - recommendation: How to fix it
        - line_numbers: Affected lines (if applicable)
        
        Return only valid JSON without markdown formatting.
        """
        
        try:
            response = self.model.generate_content(prompt)
            # Parse JSON response
            issues = json.loads(response.text)
            return issues if isinstance(issues, list) else [issues]
        except Exception as e:
            logging.error(f"Error analyzing security: {e}")
            return [{"severity": "low", "description": f"Analysis error: {str(e)}", "recommendation": "Manual review required"}]


class GitHubIntegration:
    """GitHub API integration for repository operations"""
    
    def __init__(self, token: str):
        self.github = Github(token)
        self.token = token
        
        logging.info("Initialized GitHub integration")
    
    def get_pull_request_diff(self, repo_name: str, pr_number: int) -> str:
        """Get diff content for a pull request"""
        try:
            repo = self.github.get_repo(repo_name)
            pr = repo.get_pull(pr_number)
            
            # Get diff using GitHub API
            headers = {
                "Authorization": f"token {self.token}",
                "Accept": "application/vnd.github.v3.diff"
            }
            
            response = requests.get(pr.diff_url, headers=headers)
            response.raise_for_status()
            
            return response.text
        except Exception as e:
            logging.error(f"Error getting PR diff: {e}")
            return ""
    
    def post_review_comment(self, repo_name: str, pr_number: int, comment: str) -> bool:
        """Post a review comment on a pull request"""
        try:
            repo = self.github.get_repo(repo_name)
            pr = repo.get_pull(pr_number)
            
            # Create review
            pr.create_review(body=comment, event="COMMENT")
            
            logging.info(f"Posted review comment on PR #{pr_number}")
            return True
        except Exception as e:
            logging.error(f"Error posting comment: {e}")
            return False
    
    def create_issue_comment(self, repo_name: str, issue_number: int, comment: str) -> bool:
        """Create a comment on an issue"""
        try:
            repo = self.github.get_repo(repo_name)
            issue = repo.get_issue(issue_number)
            
            issue.create_comment(comment)
            
            logging.info(f"Posted comment on issue #{issue_number}")
            return True
        except Exception as e:
            logging.error(f"Error posting issue comment: {e}")
            return False
    
    def get_repository_files(self, repo_name: str, file_extensions: List[str] = None) -> List[Dict[str, str]]:
        """Get repository files for analysis"""
        try:
            repo = self.github.get_repo(repo_name)
            contents = repo.get_contents("")
            
            files = []
            for content_file in contents:
                if content_file.type == "file":
                    if not file_extensions or any(content_file.name.endswith(ext) for ext in file_extensions):
                        files.append({
                            "path": content_file.path,
                            "content": content_file.decoded_content.decode('utf-8'),
                            "size": content_file.size
                        })
            
            return files
        except Exception as e:
            logging.error(f"Error getting repository files: {e}")
            return []


class GeminiGitHubBot:
    """Main bot orchestrating Gemini and GitHub integration"""
    
    def __init__(self, project_id: str, github_token: str, location: str = "us-central1"):
        self.gemini = GeminiClient(project_id, location)
        self.github = GitHubIntegration(github_token)
        
        logging.info("Initialized Gemini GitHub Bot")
    
    async def handle_pull_request(self, event: GitHubEvent) -> bool:
        """Handle pull request events"""
        if event.action not in ["opened", "synchronize"]:
            return True
        
        repo_name = event.data["repository"]["full_name"]
        pr_number = event.data["pull_request"]["number"]
        
        logging.info(f"Processing PR #{pr_number} in {repo_name}")
        
        # Get PR diff
        diff_content = self.github.get_pull_request_diff(repo_name, pr_number)
        if not diff_content:
            return False
        
        # Analyze with Gemini
        analysis = await self.gemini.analyze_code_changes(
            diff_content, 
            f"PR #{pr_number}"
        )
        
        # Post review comment
        comment = f"""
## 🤖 AI Code Review by Gemini

{analysis}

---
*This review was automatically generated by Gemini AI. Please verify all suggestions before implementing.*
        """
        
        return self.github.post_review_comment(repo_name, pr_number, comment)
    
    async def handle_issue(self, event: GitHubEvent) -> bool:
        """Handle issue events"""
        if event.action != "opened":
            return True
        
        repo_name = event.data["repository"]["full_name"]
        issue_number = event.data["issue"]["number"]
        issue_title = event.data["issue"]["title"]
        issue_body = event.data["issue"]["body"] or ""
        
        logging.info(f"Processing issue #{issue_number} in {repo_name}")
        
        # Generate AI response
        prompt = f"""
        Analyze this GitHub issue and provide helpful suggestions:
        
        Title: {issue_title}
        Description: {issue_body}
        
        Provide:
        1. Issue classification (bug/feature/question/documentation)
        2. Potential solutions or next steps
        3. Related documentation or resources
        4. Priority assessment
        
        Be helpful and constructive.
        """
        
        try:
            response = await self.gemini.model.generate_content(prompt)
            
            comment = f"""
## 🤖 AI Issue Analysis

{response.text}

---
*This analysis was automatically generated by Gemini AI.*
            """
            
            return self.github.create_issue_comment(repo_name, issue_number, comment)
        except Exception as e:
            logging.error(f"Error handling issue: {e}")
            return False


def setup_logging():
    """Configure logging"""
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )


# Configuration
def get_config() -> Dict[str, str]:
    """Get configuration from environment variables"""
    return {
        "project_id": os.getenv("PROJECT_ID"),
        "github_token": os.getenv("GITHUB_TOKEN"),
        "location": os.getenv("LOCATION", "us-central1"),
    }


if __name__ == "__main__":
    setup_logging()
    config = get_config()
    
    if not all(config.values()):
        logging.error("Missing required environment variables")
        exit(1)
    
    bot = GeminiGitHubBot(
        project_id=config["project_id"],
        github_token=config["github_token"],
        location=config["location"]
    )
    
    logging.info("Gemini GitHub Bot initialized successfully")
