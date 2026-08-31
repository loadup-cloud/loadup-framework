package io.github.loadup.components.scheduler.model;

import java.util.Map;

/**
 * Payload of one scheduled run.
 *
 * @param taskName the task name
 * @param args the payload registered with the schedule
 */
public record SchedulerContext(String taskName, Map<String, String> args) {}
