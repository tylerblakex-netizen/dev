"""
Test suite for Gemini GitHub Integration
"""

import pytest
import json
from unittest.mock import Mock, patch, AsyncMock
from datetime import datetime

from gemini_github_bot import GeminiClient, GitHubIntegration, GeminiGitHubBot, GitHubEvent
from config import Config


@pytest.fixture
def sample_config():
    """Sample configuration for testing"""
    return Config(
        project_id="test-project",
        github_token="test-token",
        location="us-central1"
    )


@pytest.fixture
def sample_github_event():
    """Sample GitHub event for testing"""
    return GitHubEvent(
        event_type="pull_request",
        repository="test-owner/test-repo",
        action="opened",
        data={
            "repository": {"full_name": "test-owner/test-repo"},
            "pull_request": {"number": 123},
            "action": "opened"
        }
    )


class TestGeminiClient:
    """Test cases for GeminiClient"""
    
    @patch('vertexai.init')
    @patch('vertexai.generative_models.GenerativeModel')
    def test_gemini_client_initialization(self, mock_model, mock_init, sample_config):
        """Test GeminiClient initialization"""
        client = GeminiClient(sample_config.project_id, sample_config.location)
        
        mock_init.assert_called_once_with(
            project=sample_config.project_id, 
            location=sample_config.location
        )
        assert client.project_id == sample_config.project_id
        assert client.location == sample_config.location
    
    @patch('vertexai.generative_models.GenerativeModel')
    @patch('vertexai.init')
    async def test_analyze_code_changes(self, mock_init, mock_model_class, sample_config):
        """Test code analysis functionality"""
        # Setup
        mock_model = Mock()
        mock_response = Mock()
        mock_response.text = "This is a test analysis"
        mock_model.generate_content.return_value = mock_response
        mock_model_class.return_value = mock_model
        
        client = GeminiClient(sample_config.project_id, sample_config.location)
        
        # Test
        result = await client.analyze_code_changes("test diff", "test.py")
        
        # Assertions
        assert result == "This is a test analysis"
        mock_model.generate_content.assert_called_once()
    
    @patch('vertexai.generative_models.GenerativeModel')
    @patch('vertexai.init')
    async def test_security_analysis(self, mock_init, mock_model_class, sample_config):
        """Test security analysis functionality"""
        # Setup
        mock_model = Mock()
        mock_response = Mock()
        mock_response.text = json.dumps([{
            "severity": "high",
            "description": "SQL injection vulnerability",
            "recommendation": "Use parameterized queries"
        }])
        mock_model.generate_content.return_value = mock_response
        mock_model_class.return_value = mock_model
        
        client = GeminiClient(sample_config.project_id, sample_config.location)
        
        # Test
        result = await client.analyze_security_issues("test code")
        
        # Assertions
        assert len(result) == 1
        assert result[0]["severity"] == "high"
        assert "SQL injection" in result[0]["description"]


class TestGitHubIntegration:
    """Test cases for GitHubIntegration"""
    
    @patch('github.Github')
    def test_github_integration_initialization(self, mock_github, sample_config):
        """Test GitHubIntegration initialization"""
        integration = GitHubIntegration(sample_config.github_token)
        
        mock_github.assert_called_once_with(sample_config.github_token)
        assert integration.token == sample_config.github_token
    
    @patch('requests.get')
    @patch('github.Github')
    def test_get_pull_request_diff(self, mock_github, mock_requests, sample_config):
        """Test getting PR diff"""
        # Setup
        mock_repo = Mock()
        mock_pr = Mock()
        mock_pr.diff_url = "https://api.github.com/repos/test/test/pulls/123.diff"
        mock_repo.get_pull.return_value = mock_pr
        mock_github_instance = Mock()
        mock_github_instance.get_repo.return_value = mock_repo
        mock_github.return_value = mock_github_instance
        
        mock_response = Mock()
        mock_response.text = "test diff content"
        mock_response.raise_for_status.return_value = None
        mock_requests.return_value = mock_response
        
        integration = GitHubIntegration(sample_config.github_token)
        
        # Test
        result = integration.get_pull_request_diff("test-owner/test-repo", 123)
        
        # Assertions
        assert result == "test diff content"
        mock_repo.get_pull.assert_called_once_with(123)
    
    @patch('github.Github')
    def test_post_review_comment(self, mock_github, sample_config):
        """Test posting review comment"""
        # Setup
        mock_repo = Mock()
        mock_pr = Mock()
        mock_repo.get_pull.return_value = mock_pr
        mock_github_instance = Mock()
        mock_github_instance.get_repo.return_value = mock_repo
        mock_github.return_value = mock_github_instance
        
        integration = GitHubIntegration(sample_config.github_token)
        
        # Test
        result = integration.post_review_comment("test-owner/test-repo", 123, "test comment")
        
        # Assertions
        assert result is True
        mock_pr.create_review.assert_called_once_with(body="test comment", event="COMMENT")


class TestGeminiGitHubBot:
    """Test cases for GeminiGitHubBot"""
    
    @patch('gemini_github_bot.GitHubIntegration')
    @patch('gemini_github_bot.GeminiClient')
    def test_bot_initialization(self, mock_gemini, mock_github, sample_config):
        """Test bot initialization"""
        bot = GeminiGitHubBot(
            sample_config.project_id,
            sample_config.github_token,
            sample_config.location
        )
        
        mock_gemini.assert_called_once_with(sample_config.project_id, sample_config.location)
        mock_github.assert_called_once_with(sample_config.github_token)
    
    @patch('gemini_github_bot.GitHubIntegration')
    @patch('gemini_github_bot.GeminiClient')
    async def test_handle_pull_request(self, mock_gemini_class, mock_github_class, 
                                       sample_config, sample_github_event):
        """Test pull request handling"""
        # Setup
        mock_gemini = Mock()
        mock_gemini.analyze_code_changes = AsyncMock(return_value="Test analysis")
        mock_gemini_class.return_value = mock_gemini
        
        mock_github = Mock()
        mock_github.get_pull_request_diff.return_value = "test diff"
        mock_github.post_review_comment.return_value = True
        mock_github_class.return_value = mock_github
        
        bot = GeminiGitHubBot(
            sample_config.project_id,
            sample_config.github_token,
            sample_config.location
        )
        
        # Test
        result = await bot.handle_pull_request(sample_github_event)
        
        # Assertions
        assert result is True
        mock_github.get_pull_request_diff.assert_called_once()
        mock_gemini.analyze_code_changes.assert_called_once()
        mock_github.post_review_comment.assert_called_once()


class TestConfig:
    """Test cases for configuration"""
    
    def test_config_validation(self):
        """Test configuration validation"""
        # Valid config
        valid_config = Config(
            project_id="test-project",
            github_token="test-token"
        )
        assert valid_config.validate() is True
        
        # Invalid config - missing project_id
        invalid_config = Config(
            project_id="",
            github_token="test-token"
        )
        assert invalid_config.validate() is False
        
        # Invalid config - missing github_token
        invalid_config2 = Config(
            project_id="test-project",
            github_token=""
        )
        assert invalid_config2.validate() is False
    
    @patch.dict('os.environ', {
        'PROJECT_ID': 'test-project',
        'GITHUB_TOKEN': 'test-token',
        'LOCATION': 'us-west1'
    })
    def test_config_from_env(self):
        """Test configuration from environment variables"""
        config = Config.from_env()
        
        assert config.project_id == "test-project"
        assert config.github_token == "test-token"
        assert config.location == "us-west1"


class TestGitHubEvent:
    """Test cases for GitHubEvent"""
    
    def test_github_event_creation(self):
        """Test GitHub event creation"""
        event = GitHubEvent(
            event_type="pull_request",
            repository="test-owner/test-repo",
            action="opened",
            data={"test": "data"}
        )
        
        assert event.event_type == "pull_request"
        assert event.repository == "test-owner/test-repo"
        assert event.action == "opened"
        assert event.data == {"test": "data"}
        assert isinstance(event.timestamp, datetime)


# Integration tests
class TestIntegration:
    """Integration test cases"""
    
    @pytest.mark.asyncio
    async def test_end_to_end_pr_flow(self, sample_config, sample_github_event):
        """Test end-to-end pull request flow"""
        with patch('vertexai.init'), \
             patch('vertexai.generative_models.GenerativeModel') as mock_model_class, \
             patch('github.Github') as mock_github_class, \
             patch('requests.get') as mock_requests:
            
            # Setup mocks
            mock_model = Mock()
            mock_model.generate_content.return_value.text = "Test analysis"
            mock_model_class.return_value = mock_model
            
            mock_repo = Mock()
            mock_pr = Mock()
            mock_pr.diff_url = "https://test.com/diff"
            mock_repo.get_pull.return_value = mock_pr
            mock_github_instance = Mock()
            mock_github_instance.get_repo.return_value = mock_repo
            mock_github_class.return_value = mock_github_instance
            
            mock_response = Mock()
            mock_response.text = "test diff content"
            mock_response.raise_for_status.return_value = None
            mock_requests.return_value = mock_response
            
            # Create bot and test
            bot = GeminiGitHubBot(
                sample_config.project_id,
                sample_config.github_token,
                sample_config.location
            )
            
            result = await bot.handle_pull_request(sample_github_event)
            
            # Verify the flow completed successfully
            assert result is True


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
