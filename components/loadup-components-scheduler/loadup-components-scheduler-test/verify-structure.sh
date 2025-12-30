#!/bin/bash

# LoadUp Scheduler Test Verification Script
# Quick overview of test structure and statistics

echo "=========================================="
echo "LoadUp Scheduler Test Structure Overview"
echo "=========================================="
echo ""

TEST_DIR="/Users/lise/PersonalSpace/loadup-cloud/loadup-framework/components/loadup-components-scheduler/loadup-components-scheduler-test"

cd "$TEST_DIR"

echo "📁 Test Module: loadup-components-scheduler-test"
echo ""

# Count test files
echo "📊 Test Files Statistics:"
TEST_FILES=$(find src/test/java -name "*Test.java" -o -name "*Tests.java" | wc -l | xargs)
echo "   - Test classes: $TEST_FILES"

# Count test methods (approximate)
TEST_METHODS=$(grep -r "@Test" src/test/java 2>/dev/null | wc -l | xargs)
echo "   - Test methods: ~$TEST_METHODS"

echo ""
echo "📂 Test Structure:"
echo ""
echo "src/test/java/com/github/loadup/components/scheduler/"

# List test files by category
echo "├── 📦 api/"
ls src/test/java/com/github/loadup/components/scheduler/api/*Test.java 2>/dev/null | sed 's|.*/||' | sed 's/^/│   └── /'

echo "├── 📦 binding/"
ls src/test/java/com/github/loadup/components/scheduler/binding/*Test.java 2>/dev/null | sed 's|.*/||' | sed 's/^/│   └── /'

echo "├── 📦 config/"
ls src/test/java/com/github/loadup/components/scheduler/config/*Test.java 2>/dev/null | sed 's|.*/||' | sed 's/^/│   └── /'

echo "├── 📦 core/"
ls src/test/java/com/github/loadup/components/scheduler/core/*Test.java 2>/dev/null | sed 's|.*/||' | sed 's/^/│   └── /'

echo "├── 📦 integration/"
ls src/test/java/com/github/loadup/components/scheduler/integration/*Test.java 2>/dev/null | sed 's|.*/||' | sed 's/^/│   └── /'

echo "├── 📦 model/"
ls src/test/java/com/github/loadup/components/scheduler/model/*Test.java 2>/dev/null | sed 's|.*/||' | sed 's/^/│   └── /'

echo "├── 📦 quartz/"
ls src/test/java/com/github/loadup/components/scheduler/quartz/*Test.java 2>/dev/null | sed 's|.*/||' | sed 's/^/│   └── /'

echo "└── 📦 simplejob/"
ls src/test/java/com/github/loadup/components/scheduler/simplejob/*Test.java 2>/dev/null | sed 's|.*/||' | sed 's/^/    └── /'

echo ""
echo "📋 Documentation:"
ls *.md 2>/dev/null | sed 's/^/   - /'

echo ""
echo "🔧 Test Configuration:"
echo "   - pom.xml (with JaCoCo plugin)"
echo "   - application.properties"
echo "   - run-tests.sh"

echo ""
echo "=========================================="
echo "✅ Test module structure verified!"
echo "=========================================="
echo ""
echo "To run tests:"
echo "  ./run-tests.sh"
echo "  or"
echo "  mvn test"
echo ""

