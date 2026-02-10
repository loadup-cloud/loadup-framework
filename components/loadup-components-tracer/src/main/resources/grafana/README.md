# LoadUp Framework Metrics Integration

This directory contains the Grafana dashboard configuration for monitoring LoadUp Framework applications.

## Overview

The LoadUp Framework provides comprehensive metrics integration through Micrometer and Prometheus, including:

- **HikariCP Metrics**: Connection pool monitoring (active, idle, pending connections)
- **Lettuce (Redis) Metrics**: Redis command execution and connection metrics
- **ThreadPool Metrics**: Thread pool size, active threads, and queued tasks
- **JVM Metrics**: Memory, GC, and thread metrics (provided by Micrometer)

## Configuration

### 1. Enable Metrics in application.yml

```yaml
loadup:
  tracer:
    enabled: true
    metrics-enabled: true  # Enable metrics collection (default: true)

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. Prometheus Configuration

Add the following to your `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'loadup-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### 3. Import Grafana Dashboard

1. Open Grafana UI
2. Go to Dashboards → Import
3. Upload the `loadup-framework-dashboard.json` file
4. Configure the Prometheus datasource
5. Save the dashboard

## Available Metrics

### HikariCP Connection Pool Metrics

- `hikaricp_connections_active` - Number of active connections
- `hikaricp_connections_idle` - Number of idle connections
- `hikaricp_connections_pending` - Number of pending connections
- `hikaricp_connections_timeout_total` - Total connection timeouts
- `hikaricp_connections_creation_seconds` - Connection creation time

### Redis (Lettuce) Metrics

- `lettuce_command_completion_duration_seconds` - Command execution duration
- `lettuce_command_completion_duration_seconds_count` - Command execution count
- `lettuce_command_completion_duration_seconds_sum` - Total command execution time

### ThreadPool Metrics

- `executor_pool_size_threads` - Thread pool size
- `executor_active_threads` - Number of active threads
- `executor_queued_tasks` - Number of queued tasks
- `executor_completed_tasks_total` - Total completed tasks

## Custom Metrics

You can add custom metrics using the `MeterRegistry`:

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final MeterRegistry meterRegistry;
    
    public void processRequest() {
        Counter counter = meterRegistry.counter("my.custom.counter", "type", "request");
        counter.increment();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        // ... processing logic ...
        sample.stop(meterRegistry.timer("my.custom.timer", "type", "processing"));
    }
}
```

## Troubleshooting

### Metrics not appearing in Prometheus

1. Verify the `/actuator/prometheus` endpoint is accessible
2. Check that `management.endpoints.web.exposure.include` includes `prometheus`
3. Verify Prometheus is scraping the correct target

### Dashboard shows no data

1. Ensure the Prometheus datasource is correctly configured in Grafana
2. Verify the `application` label matches your application name
3. Check that metrics are being exported from your application

## Resources

- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
