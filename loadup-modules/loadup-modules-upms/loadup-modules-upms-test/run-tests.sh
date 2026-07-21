#!/bin/bash

set -e

echo "======================================"
echo "UPMS Test - Docker Diagnostics"
echo "======================================"
echo ""

# Check if Docker command exists
if ! command -v docker &> /dev/null; then
    echo "❌ Docker command not found!"
    echo "Please install Docker Desktop or OrbStack"
    exit 1
fi

echo "✓ Docker command found: $(which docker)"
echo ""

# Check Docker version
echo "Checking Docker version..."
docker version > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Docker daemon is running"
    docker version --format '{{.Server.Version}}' 2>/dev/null | head -1
else
    echo "❌ Docker daemon is NOT running!"
    echo ""
    echo "Please start Docker:"
    echo "  - OrbStack: open -a OrbStack"
    echo "  - Docker Desktop: open -a Docker"
    exit 1
fi

echo ""

# Check Docker socket
echo "Checking Docker socket..."
if [ -S /var/run/docker.sock ]; then
    echo "✓ Standard socket: /var/run/docker.sock"
elif [ -S ~/.orbstack/run/docker.sock ]; then
    echo "✓ OrbStack socket: ~/.orbstack/run/docker.sock"
    echo "  Note: Testcontainers should auto-detect this"
else
    echo "⚠ No Docker socket found at standard locations"
fi

echo ""

# Test Docker with a simple container
echo "Testing Docker with hello-world container..."
if docker run --rm hello-world > /dev/null 2>&1; then
    echo "✓ Docker is working correctly"
else
    echo "❌ Docker test failed"
    exit 1
fi

echo ""
echo "======================================"
echo "Running UPMS Tests"
echo "======================================"
echo ""

# Run the tests
cd "$(dirname "$0")"
mvn clean test "$@"

