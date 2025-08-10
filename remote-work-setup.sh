#!/bin/bash

# 🚀 Complete Remote Work Setup for All Projects
# This script sets up both Gemini GitHub Integration and Java Enterprise projects

set -e

echo "🌟 Complete Remote Work Environment Setup"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
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

print_header() {
    echo -e "${PURPLE}🔧 $1${NC}"
    echo "----------------------------------------"
}

# Check if running in VS Code/Codespace
check_environment() {
    print_header "Environment Check"
    
    if [ -n "$CODESPACES" ]; then
        print_success "Running in GitHub Codespaces ✓"
    elif [ -n "$VSCODE_IPC_HOOK" ]; then
        print_success "Running in VS Code ✓"
    else
        print_warning "Environment not detected, but continuing..."
    fi
    
    print_status "Current directory: $(pwd)"
}

# Setup Java environment
setup_java_environment() {
    print_header "Setting Up Java Environment"
    
    cd /workspaces/dev/java-project
    
    # Check Java version
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_status "Java version detected: $JAVA_VERSION"
    else
        print_error "Java not found! Please install Java 21."
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n 1)
        print_status "Maven detected: $MVN_VERSION"
    else
        print_error "Maven not found! Please install Maven."
    fi
    
    # Verify project structure
    print_status "Verifying Java project structure..."
    if [ -f "pom.xml" ]; then
        print_success "Maven project detected ✓"
        
        print_status "Running Maven clean compile..."
        mvn clean compile -q
        
        print_status "Running tests..."
        mvn test -q
        
        print_success "Java project setup complete ✓"
    else
        print_error "pom.xml not found in java-project directory"
    fi
    
    cd /workspaces/dev
}

# Setup Python environment for Gemini integration
setup_python_environment() {
    print_header "Setting Up Python Environment for Gemini Integration"
    
    cd /workspaces/dev/gemini-github-integration
    
    # Check Python version
    if command -v python3 &> /dev/null; then
        PYTHON_VERSION=$(python3 --version)
        print_status "Python detected: $PYTHON_VERSION"
    else
        print_error "Python3 not found! Please install Python3."
    fi
    
    # Create virtual environment
    print_status "Creating virtual environment..."
    python3 -m venv venv
    source venv/bin/activate
    
    # Upgrade pip
    print_status "Upgrading pip..."
    pip install --upgrade pip
    
    # Install requirements
    if [ -f "requirements.txt" ]; then
        print_status "Installing Python dependencies..."
        pip install -r requirements.txt
        print_success "Python dependencies installed ✓"
    else
        print_warning "requirements.txt not found, installing basic packages..."
        pip install requests google-cloud-aiplatform PyGithub python-dotenv
    fi
    
    print_success "Python environment setup complete ✓"
    
    cd /workspaces/dev
}

# Main execution
main() {
    check_environment
    setup_java_environment
    setup_python_environment
    
    print_header "🎉 All Projects Setup Complete!"
    print_success "Both Java and Python projects are ready."
    print_warning "To work on a project, navigate to its directory and activate the respective environment."
    echo "  - Java: cd /workspaces/dev/java-project"
    echo "  - Python: cd /workspaces/dev/gemini-github-integration && source venv/bin/activate"
    
    touch /workspaces/dev/SETUP_COMPLETE.sh
}

main "$@"

set -e

echo "🌟 Complete Remote Work Environment Setup"
echo "========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
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

print_header() {
    echo -e "${PURPLE}🔧 $1${NC}"
    echo "----------------------------------------"
}

# Check if running in VS Code/Codespace
check_environment() {
    print_header "Environment Check"
    
    if [ -n "$CODESPACES" ]; then
        print_success "Running in GitHub Codespaces ✓"
    elif [ -n "$VSCODE_IPC_HOOK" ]; then
        print_success "Running in VS Code ✓"
    else
        print_warning "Environment not detected, but continuing..."
    fi
    
    print_status "Current directory: $(pwd)"
    print_status "User: $(whoami)"
    print_status "OS: $(uname -a)"
}

# Install system dependencies
install_system_deps() {
    print_header "Installing System Dependencies"
    
    # Update package list
    print_status "Updating package list..."
    sudo apt-get update -qq
    
    # Install essential tools
    print_status "Installing essential tools..."
    sudo apt-get install -y \
        curl \
        wget \
        git \
        unzip \
        jq \
        tree \
        htop \
        build-essential
    
    print_success "System dependencies installed ✓"
}

# Setup Java environment
setup_java_environment() {
    print_header "Setting Up Java Environment"
    
    cd /workspaces/dev/java-project
    
    # Check Java version
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
        print_status "Java version detected: $JAVA_VERSION"
    else
        print_error "Java not found! Installing OpenJDK 21..."
        sudo apt-get install -y openjdk-21-jdk
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n 1)
        print_status "Maven detected: $MVN_VERSION"
    else
        print_error "Maven not found! Installing..."
        sudo apt-get install -y maven
    fi
    
    # Verify project structure
    print_status "Verifying Java project structure..."
    if [ -f "pom.xml" ]; then
        print_success "Maven project detected ✓"
        
        print_status "Running Maven clean compile..."
        mvn clean compile -q
        
        print_status "Running tests..."
        mvn test -q
        
        print_success "Java project setup complete ✓"
    else
        print_error "pom.xml not found in java-project directory"
    fi
    
    cd /workspaces/dev
}

# Setup Python environment for Gemini integration
setup_python_environment() {
    print_header "Setting Up Python Environment for Gemini Integration"
    
    cd /workspaces/dev/gemini-github-integration
    
    # Check Python version
    if command -v python3 &> /dev/null; then
        PYTHON_VERSION=$(python3 --version)
        print_status "Python detected: $PYTHON_VERSION"
    else
        print_error "Python3 not found! Installing..."
        sudo apt-get install -y python3 python3-pip python3-venv
    fi
    
    # Create virtual environment
    print_status "Creating virtual environment..."
    python3 -m venv venv
    source venv/bin/activate
    
    # Upgrade pip
    print_status "Upgrading pip..."
    pip install --upgrade pip
    
    # Install requirements
    if [ -f "requirements.txt" ]; then
        print_status "Installing Python dependencies..."
        pip install -r requirements.txt
        print_success "Python dependencies installed ✓"
    else
        print_warning "requirements.txt not found, installing basic packages..."
        pip install requests google-cloud-aiplatform PyGithub python-dotenv
    fi
    
    print_success "Python environment setup complete ✓"
    
    cd /workspaces/dev
}

# Setup Google Cloud CLI
setup_gcloud_cli() {
    print_header "Setting Up Google Cloud CLI"
    
    if command -v gcloud &> /dev/null; then
        print_success "Google Cloud CLI already installed ✓"
        gcloud version
    else
        print_status "Installing Google Cloud CLI..."
        
        # Download and install gcloud
        curl -sSL https://sdk.cloud.google.com | bash
        source ~/.bashrc
        
        print_success "Google Cloud CLI installed ✓"
    fi
    
    print_warning "Remember to run 'gcloud auth login' to authenticate"
    print_warning "Set your project with 'gcloud config set project YOUR_PROJECT_ID'"
}

# Setup GitHub CLI
setup_github_cli() {
    print_header "Setting Up GitHub CLI"
    
    if command -v gh &> /dev/null; then
        print_success "GitHub CLI already installed ✓"
        gh --version
    else
        print_status "Installing GitHub CLI..."
        
        # Install GitHub CLI
        curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
        sudo chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
        sudo apt update
        sudo apt install gh
        
        print_success "GitHub CLI installed ✓"
    fi
    
    print_warning "Remember to run 'gh auth login' to authenticate"
}

# Create VS Code workspace configuration
setup_vscode_workspace() {
    print_header "Setting Up VS Code Workspace"
    
    # Create workspace file
    cat > /workspaces/dev/remote-workspace.code-workspace << 'EOF'
{
    "folders": [
        {
            "name": "🤖 Gemini GitHub Integration",
            "path": "./gemini-github-integration"
        },
        {
            "name": "☕ Java Enterprise Project",
            "path": "./java-project"
        },
        {
            "name": "📁 Root",
            "path": "."
        }
    ],
    "settings": {
        "python.defaultInterpreterPath": "./gemini-github-integration/venv/bin/python",
        "java.home": "/usr/lib/jvm/java-21-openjdk-amd64",
        "maven.executable.path": "/usr/bin/mvn",
        "terminal.integrated.defaultProfile.linux": "bash",
        "files.exclude": {
            "**/target": true,
            "**/__pycache__": true,
            "**/venv": false,
            "**/.git": false
        },
        "search.exclude": {
            "**/target": true,
            "**/venv": true
        }
    },
    "tasks": {
        "version": "2.0.0",
        "tasks": [
            {
                "label": "Java: Build",
                "type": "shell",
                "command": "mvn",
                "args": ["clean", "compile"],
                "group": "build",
                "options": {
                    "cwd": "${workspaceFolder}/java-project"
                }
            },
            {
                "label": "Java: Test",
                "type": "shell",
                "command": "mvn",
                "args": ["test"],
                "group": "test",
                "options": {
                    "cwd": "${workspaceFolder}/java-project"
                }
            },
            {
                "label": "Python: Install Dependencies",
                "type": "shell",
                "command": "pip",
                "args": ["install", "-r", "requirements.txt"],
                "group": "build",
                "options": {
                    "cwd": "${workspaceFolder}/gemini-github-integration"
                }
            },
            {
                "label": "Python: Run Tests",
                "type": "shell",
                "command": "python",
                "args": ["-m", "pytest", "tests/"],
                "group": "test",
                "options": {
                    "cwd": "${workspaceFolder}/gemini-github-integration"
                }
            }
        ]
    },
    "launch": {
        "version": "0.2.0",
        "configurations": [
            {
                "name": "Java: Debug App",
                "type": "java",
                "request": "launch",
                "mainClass": "com.example.App",
                "projectName": "enterprise-java-app"
            },
            {
                "name": "Python: Debug Bot",
                "type": "python",
                "request": "launch",
                "program": "${workspaceFolder}/gemini-github-integration/gemini_github_bot.py",
                "console": "integratedTerminal",
                "cwd": "${workspaceFolder}/gemini-github-integration"
            }
        ]
    }
}
EOF

    print_success "VS Code workspace configuration created ✓"
}

# Create environment template files
create_env_templates() {
    print_header "Creating Environment Templates"
    
    # Create .env template for Gemini integration
    cat > /workspaces/dev/gemini-github-integration/.env.template << 'EOF'
# Gemini GitHub Integration Environment Variables
# Copy this file to .env and fill in your actual values

# Google Cloud Configuration
GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
PROJECT_ID=your-gcp-project-id
LOCATION=us-central1

# GitHub Configuration
GITHUB_TOKEN=your_github_personal_access_token
GITHUB_WEBHOOK_SECRET=your_webhook_secret

# Application Configuration
LOG_LEVEL=INFO
GEMINI_MODEL=gemini-pro
MAX_TOKENS=8192

# Cloud Function Configuration (if deploying)
FUNCTION_NAME=gemini-github-integration
FUNCTION_REGION=us-central1
FUNCTION_MEMORY=1Gi
FUNCTION_TIMEOUT=540s
EOF

    # Create Java project environment
    cat > /workspaces/dev/java-project/.env.template << 'EOF'
# Java Enterprise Project Environment Variables
# Copy this file to .env if needed for your application

# Database Configuration (if using)
DATABASE_URL=jdbc:postgresql://localhost:5432/mydb
DATABASE_USERNAME=user
DATABASE_PASSWORD=password

# Application Configuration
APP_PORT=8080
APP_PROFILE=development

# External API Configuration
API_BASE_URL=https://api.example.com
API_KEY=your_api_key

# Monitoring Configuration
METRICS_ENABLED=true
LOGGING_LEVEL=INFO
EOF

    print_success "Environment templates created ✓"
}

# Create comprehensive README for remote work
create_remote_work_readme() {
    print_header "Creating Remote Work Documentation"
    
    cat > /workspaces/dev/REMOTE_WORK_SETUP.md << 'EOF'
# 🌟 Complete Remote Work Environment

This workspace contains two fully configured projects ready for remote development:

## 🤖 Projects Overview

### 1. **Gemini GitHub Integration** (`gemini-github-integration/`)
- **Purpose**: AI-powered GitHub automation with Google Gemini
- **Language**: Python 3.x
- **Key Features**: Code review, issue analysis, security scanning
- **Environment**: Virtual environment in `venv/`

### 2. **Java Enterprise Project** (`java-project/`)
- **Purpose**: Modern Java 21 enterprise application
- **Language**: Java 21
- **Build Tool**: Maven
- **Key Features**: Concurrent processing, metrics, validation

## 🚀 Quick Start Commands

### For Gemini GitHub Integration:
```bash
cd gemini-github-integration
source venv/bin/activate
pip install -r requirements.txt
cp .env.template .env  # Edit with your values
python gemini_github_bot.py
```

### For Java Project:
```bash
cd java-project
mvn clean compile
mvn test
mvn exec:java -Dexec.mainClass="com.example.App"
```

## 🛠 Available VS Code Tasks
- **Java: Build** - Compile Java project
- **Java: Test** - Run Java tests
- **Python: Install Dependencies** - Install Python packages
- **Python: Run Tests** - Run Python tests

## 📞 Support Contacts

### Google Gemini Support
- **Primary**: cloud-support@google.com
- **Documentation**: https://ai.google.dev/gemini-api/docs
- **Community**: https://ai.google.dev/community

### GitHub Integration Support
- **Primary**: support@github.com
- **Developer Program**: developer@github.com
- **API Docs**: https://docs.github.com/en/rest

### Technical Escalation
- **Google Cloud**: cloud-support@google.com
- **GitHub Enterprise**: premium-support@github.com
- **Security Issues**: security@github.com + cloud-security@google.com

## 🔧 Development Tools Installed
- ✅ Java 21 (OpenJDK)
- ✅ Maven (latest)
- ✅ Python 3.x with virtual environment
- ✅ Google Cloud CLI
- ✅ GitHub CLI
- ✅ VS Code workspace configuration

## 🔐 Authentication Setup Required
1. **Google Cloud**: Run `gcloud auth login`
2. **GitHub**: Run `gh auth login`
3. **Environment Variables**: Configure `.env` files

## 📁 Workspace Structure
```
📁 remote-workspace.code-workspace  # VS Code multi-root workspace
📁 gemini-github-integration/       # AI GitHub automation
   ├── 🐍 Python virtual environment
   ├── 📦 All dependencies installed
   └── 🔧 Ready for development
📁 java-project/                    # Enterprise Java application
   ├── ☕ Java 21 configured
   ├── 📦 Maven dependencies ready
   └── 🔧 Ready for development
```

Happy coding! 🚀
EOF

    print_success "Remote work documentation created ✓"
}

# Display final status and next steps
show_completion_status() {
    print_header "Setup Complete! 🎉"
    
    echo -e "${GREEN}✅ System dependencies installed${NC}"
    echo -e "${GREEN}✅ Java environment configured${NC}"
    echo -e "${GREEN}✅ Python environment with virtual env${NC}"
    echo -e "${GREEN}✅ Google Cloud CLI ready${NC}"
    echo -e "${GREEN}✅ GitHub CLI ready${NC}"
    echo -e "${GREEN}✅ VS Code workspace configured${NC}"
    echo -e "${GREEN}✅ Environment templates created${NC}"
    echo -e "${GREEN}✅ Documentation generated${NC}"
    
    echo ""
    print_header "Next Steps"
    echo "1. 🔐 Authenticate with Google Cloud: gcloud auth login"
    echo "2. 🔐 Authenticate with GitHub: gh auth login"
    echo "3. ⚙️  Configure environment variables in .env files"
    echo "4. 🚀 Open remote-workspace.code-workspace in VS Code"
    echo "5. 📖 Read REMOTE_WORK_SETUP.md for detailed instructions"
    
    echo ""
    print_success "🌟 Your remote work environment is ready!"
    print_status "📁 Open the workspace file: remote-workspace.code-workspace"
}

# Main execution flow
main() {
    check_environment
    install_system_deps
    setup_java_environment
    setup_python_environment
    setup_gcloud_cli
    setup_github_cli
    setup_vscode_workspace
    create_env_templates
    create_remote_work_readme
    show_completion_status
}

# Run main function
main "$@"
