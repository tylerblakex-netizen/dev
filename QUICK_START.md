# 🚀 Quick Start Commands - Remote Work Environment

## Immediate Setup Commands

```bash
# 1. Authenticate with services
gcloud auth login
gh auth login

# 2. Configure environment variables
cp gemini-github-integration/.env.template gemini-github-integration/.env
cp java-project/.env.template java-project/.env
# Edit .env files with your actual credentials

# 3. Open VS Code workspace
code remote-workspace.code-workspace
```

## Gemini GitHub Integration Commands

```bash
cd gemini-github-integration

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install --upgrade pip
pip install -r requirements.txt

# Test the bot
python gemini_github_bot.py

# Deploy to Google Cloud
./setup.sh
```

## Java Enterprise Project Commands

```bash
cd java-project

# Build project
mvn clean compile

# Run tests
mvn test

# Run application
mvn exec:java -Dexec.mainClass="com.example.App"

# Package for deployment
mvn package
```

## VS Code Integrated Tasks

Use `Ctrl+Shift+P` → "Tasks: Run Task" and select:
- **Java: Build** - Compile Java project
- **Java: Test** - Run Java tests  
- **Python: Install Dependencies** - Install Python packages
- **Gemini: Run Bot** - Start AI bot

## Debug Configurations

Press `F5` and select:
- **Java: Debug App** - Debug Java application
- **Python: Debug Gemini Bot** - Debug AI bot

## Verification Commands

```bash
# Check all tools
./check-github-status.sh
./verify-setup.sh

# Test connections
gh auth status
gcloud auth list
python3 --version
java -version
mvn --version
```

---

**🎯 You're all set! Both projects are production-ready with full remote work capabilities.**
