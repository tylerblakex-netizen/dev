"""
Google Cloud Function for handling GitHub webhooks
Processes GitHub events and triggers Gemini AI analysis
"""

import json
import logging
import asyncio
from typing import Dict, Any

import functions_framework
from flask import Request

from gemini_github_bot import GeminiGitHubBot, GitHubEvent, get_config, setup_logging


# Initialize global bot instance
setup_logging()
config = get_config()
bot = GeminiGitHubBot(
    project_id=config["project_id"],
    github_token=config["github_token"],
    location=config["location"]
)


@functions_framework.http
def github_webhook_handler(request: Request) -> Dict[str, Any]:
    """
    Cloud Function entry point for GitHub webhooks
    
    Processes GitHub webhook events and triggers appropriate AI analysis
    """
    
    # Verify webhook signature (implement in production)
    if not verify_github_signature(request):
        return {"error": "Invalid signature"}, 401
    
    # Parse GitHub event
    try:
        event_type = request.headers.get("X-GitHub-Event")
        payload = request.get_json()
        
        if not event_type or not payload:
            return {"error": "Invalid webhook payload"}, 400
        
        # Create event object
        github_event = GitHubEvent(
            event_type=event_type,
            repository=payload.get("repository", {}).get("full_name", ""),
            action=payload.get("action", ""),
            data=payload
        )
        
        logging.info(f"Received {event_type} event for {github_event.repository}")
        
        # Process event asynchronously
        result = asyncio.run(process_github_event(github_event))
        
        return {
            "status": "success" if result else "failed",
            "event_type": event_type,
            "repository": github_event.repository,
            "processed": result
        }
        
    except Exception as e:
        logging.error(f"Error processing webhook: {e}")
        return {"error": str(e)}, 500


async def process_github_event(event: GitHubEvent) -> bool:
    """
    Process different types of GitHub events
    """
    
    try:
        if event.event_type == "pull_request":
            return await bot.handle_pull_request(event)
        
        elif event.event_type == "issues":
            return await bot.handle_issue(event)
        
        elif event.event_type == "push":
            return await handle_push_event(event)
        
        elif event.event_type == "release":
            return await handle_release_event(event)
        
        else:
            logging.info(f"Unsupported event type: {event.event_type}")
            return True
            
    except Exception as e:
        logging.error(f"Error processing {event.event_type} event: {e}")
        return False


async def handle_push_event(event: GitHubEvent) -> bool:
    """Handle push events for automatic documentation updates"""
    
    if event.data.get("ref") != "refs/heads/main":
        return True  # Only process main branch pushes
    
    repo_name = event.repository
    commits = event.data.get("commits", [])
    
    logging.info(f"Processing push to {repo_name} with {len(commits)} commits")
    
    # Check if documentation files were added/modified
    doc_files_changed = False
    for commit in commits:
        modified_files = commit.get("modified", []) + commit.get("added", [])
        if any(f.endswith(('.md', '.rst', '.txt')) for f in modified_files):
            doc_files_changed = True
            break
    
    if doc_files_changed:
        # Generate updated documentation
        files = bot.github.get_repository_files(repo_name, ['.py', '.js', '.java', '.go'])
        
        for file_info in files[:5]:  # Limit to first 5 files
            try:
                docs = await bot.gemini.generate_documentation(
                    file_info["content"], 
                    file_info["path"]
                )
                
                # You could create a PR with updated docs here
                logging.info(f"Generated docs for {file_info['path']}")
                
            except Exception as e:
                logging.error(f"Error generating docs for {file_info['path']}: {e}")
    
    return True


async def handle_release_event(event: GitHubEvent) -> bool:
    """Handle release events to generate release notes"""
    
    if event.action != "published":
        return True
    
    repo_name = event.repository
    release = event.data.get("release", {})
    tag_name = release.get("tag_name", "")
    
    logging.info(f"Processing release {tag_name} for {repo_name}")
    
    # Generate AI-powered release notes
    prompt = f"""
    Generate comprehensive release notes for version {tag_name} based on recent commits.
    Include:
    1. New features
    2. Bug fixes
    3. Breaking changes
    4. Improvements
    5. Security updates
    
    Make it user-friendly and highlight important changes.
    """
    
    try:
        response = await bot.gemini.model.generate_content(prompt)
        
        # You could update the release description here
        logging.info(f"Generated release notes for {tag_name}")
        return True
        
    except Exception as e:
        logging.error(f"Error generating release notes: {e}")
        return False


def verify_github_signature(request: Request) -> bool:
    """
    Verify GitHub webhook signature for security
    Implement HMAC-SHA256 verification in production
    """
    
    # TODO: Implement proper signature verification
    # signature = request.headers.get("X-Hub-Signature-256")
    # payload = request.get_data()
    # secret = os.getenv("GITHUB_WEBHOOK_SECRET")
    # 
    # expected_signature = "sha256=" + hmac.new(
    #     secret.encode(), payload, hashlib.sha256
    # ).hexdigest()
    # 
    # return hmac.compare_digest(signature, expected_signature)
    
    return True  # Skip verification for demo


# Health check endpoint
@functions_framework.http
def health_check(request: Request) -> Dict[str, str]:
    """Health check endpoint"""
    return {"status": "healthy", "service": "gemini-github-integration"}


if __name__ == "__main__":
    # Local testing
    from flask import Flask
    
    app = Flask(__name__)
    app.add_url_rule("/webhook", "webhook", github_webhook_handler, methods=["POST"])
    app.add_url_rule("/health", "health", health_check, methods=["GET"])
    
    app.run(host="0.0.0.0", port=8080, debug=True)
