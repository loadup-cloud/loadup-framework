#!/bin/bash

echo "Testing Scheduler Integration Tests..."
echo ""

cd /Users/lise/PersonalSpace/loadup-cloud/loadup-framework/components/loadup-components-scheduler/loadup-components-scheduler-test

echo "Running Quartz testAnnotationBasedScheduling..."
mvn test -Dtest=QuartzSchedulerIntegrationTest#testAnnotationBasedScheduling

echo ""
echo "Running SimpleJob testAnnotationBasedScheduling..."
mvn test -Dtest=SimpleJobSchedulerIntegrationTest#testAnnotationBasedScheduling

echo ""
echo "Done!"

