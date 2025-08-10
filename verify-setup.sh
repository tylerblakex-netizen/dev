#!/bin/bash

# Quick verification script for remote work setup
echo "🔍 Verifying Remote Work Environment..."
echo "====================================="

# Check workspace structure
echo "📁 Workspace Structure:"
echo "  ✓ Gemini Integration: $([ -d "gemini-github-integration" ] && echo "✅ Found" || echo "❌ Missing")"
echo "  ✓ Java Project: $([ -d "java-project" ] && echo "✅ Found" || echo "❌ Missing")"
echo "  ✓ VS Code Workspace: $([ -f "remote-workspace.code-workspace" ] && echo "✅ Found" || echo "❌ Missing")"

# Check key files
echo ""
echo "📄 Key Files:"
echo "  ✓ Gemini Bot: $([ -f "gemini-github-integration/gemini_github_bot.py" ] && echo "✅ Found" || echo "❌ Missing")"
echo "  ✓ Java POM: $([ -f "java-project/pom.xml" ] && echo "✅ Found" || echo "❌ Missing")"
echo "  ✓ Setup Guide: $([ -f "REMOTE_WORK_GUIDE.md" ] && echo "✅ Found" || echo "❌ Missing")"

# Check if we can access basic tools
echo ""
echo "🛠 System Tools:"
echo "  ✓ Python: $(python3 --version 2>/dev/null || echo "❌ Not found")"
echo "  ✓ Java: $(java -version 2>&1 | head -n1 || echo "❌ Not found")"
echo "  ✓ Maven: $(mvn -version 2>/dev/null | head -n1 || echo "❌ Not found")"
echo "  ✓ Git: $(git --version 2>/dev/null || echo "❌ Not found")"

echo ""
echo "🎯 Next Steps:"
echo "  1. Open remote-workspace.code-workspace in VS Code"
echo "  2. Read REMOTE_WORK_GUIDE.md for detailed instructions"
echo "  3. Configure authentication (gcloud auth login, gh auth login)"
echo "  4. Set up environment variables in .env files"

echo ""
echo "🌟 Remote work environment verification complete!"
