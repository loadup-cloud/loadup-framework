#!/bin/bash

# UPMS Test Environment Setup Script
# This script helps set up the test environment with MySQL container

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "======================================"
echo "UPMS Test Environment Setup"
echo "======================================"
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "Error: Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Copy .env.example to .env if not exists
if [ ! -f .env ]; then
    echo "Creating .env file from .env.example..."
    cp .env.example .env
    echo "✓ .env file created. You can modify it if needed."
    echo ""
fi

# Start MySQL container
echo "Starting MySQL container..."
docker-compose up -d

echo ""
echo "Waiting for MySQL to be ready..."
sleep 10

# Check MySQL health
RETRIES=30
until docker exec loadup-upms-test-mysql mysqladmin ping -h localhost -uroot -proot --silent &> /dev/null; do
    RETRIES=$((RETRIES-1))
    if [ $RETRIES -eq 0 ]; then
        echo "Error: MySQL failed to start properly."
        docker-compose logs mysql
        exit 1
    fi
    echo "Waiting for MySQL to be ready... ($RETRIES retries left)"
    sleep 2
done

echo "✓ MySQL is ready!"
echo ""

# Display connection info
echo "======================================"
echo "MySQL Connection Information"
echo "======================================"
echo "Host: localhost"
echo "Port: 3306"
echo "Database: loadup_test"
echo "Username: root"
echo "Password: root"
echo ""

echo "======================================"
echo "Environment Ready!"
echo "======================================"
echo ""
echo "You can now run tests with:"
echo "  mvn clean test"
echo ""
echo "To stop the MySQL container:"
echo "  docker-compose down"
echo ""
echo "To view MySQL logs:"
echo "  docker-compose logs -f mysql"
echo ""

