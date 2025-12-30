#!/bin/bash

# LoadUp Scheduler Test Runner
# This script runs the scheduler component tests with coverage report

echo "========================================"
echo "LoadUp Scheduler Component Test Runner"
echo "========================================"

cd "$(dirname "$0")"

echo ""
echo "1. Running unit tests..."
mvn clean test -Dtest='!*Integration*' -q

if [ $? -ne 0 ]; then
    echo "❌ Unit tests failed!"
    exit 1
fi

echo "✅ Unit tests passed!"

echo ""
echo "2. Running integration tests..."
mvn test -Dtest='*Integration*' -q

if [ $? -ne 0 ]; then
    echo "❌ Integration tests failed!"
    exit 1
fi

echo "✅ Integration tests passed!"

echo ""
echo "3. Generating coverage report..."
mvn jacoco:report -q

if [ $? -eq 0 ]; then
    echo "✅ Coverage report generated!"
    echo "   Report location: target/site/jacoco/index.html"
else
    echo "⚠️  Failed to generate coverage report"
fi

echo ""
echo "========================================"
echo "All tests completed successfully!"
echo "========================================"

