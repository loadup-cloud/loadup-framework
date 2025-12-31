#!/bin/bash
#
# Install Git hooks for automatic code formatting using Spotless
#

echo "=========================================="
echo "  Installing Spotless Git Hooks"
echo "=========================================="
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    exit 1
fi

# Check if .git directory exists
if [ ! -d ".git" ]; then
    echo "Error: Not a Git repository"
    exit 1
fi

echo "Installing pre-push hook using Spotless..."
echo ""

# Use Spotless Maven Plugin to install Git hook
mvn com.diffplug.spotless:spotless-maven-plugin:3.1.0:install-git-pre-push-hook

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "  ✓ Git Hook Installed Successfully!"
    echo "=========================================="
    echo ""
    echo "Pre-push hook configured:"
    echo "  • Spotless will check formatting before every push"
    echo "  • If formatting issues exist, push will be blocked"
    echo "  • Run './spotless.sh apply' to fix formatting"
    echo ""
    echo "Manual formatting commands:"
    echo "  ./spotless.sh apply    - Format all files"
    echo "  ./spotless.sh check    - Check formatting"
    echo ""
else
    echo ""
    echo "Error: Failed to install Git hook"
    exit 1
fi

