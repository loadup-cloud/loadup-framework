package io.github.loadup.components.scheduler.model;

import java.util.Map;

public record SchedulerContext(String taskName, Map<String, String> args) {}
