# Architecture Overview

## Module Purpose

The `loadup-components-testcontainers` module provides shared TestContainers for integration testing across multiple modules. It simplifies
container management and improves test performance.

## Supported Containers

- **MySQL**: Shared container for relational database testing.
- **PostgreSQL**: For modules requiring PostgreSQL integration.
- **MongoDB**: NoSQL database support.
- **Kafka**: Message broker for event-driven tests.
- **Elasticsearch**: Full-text search and analytics.

## Design Principles

1. **Reusability**: Shared containers reduce redundancy.
2. **Performance**: Optimized startup and resource usage.
3. **Scalability**: Easily extendable to support new containers.

## Integration Points

- **UPMS Module**: Repository tests.
- **DFS Module**: Database provider tests.
- **Gotone Module**: Integration tests.
- **Cache Module**: Redis and Caffeine tests.

## Future Enhancements

- Add support for additional TestContainers.
- Automate configuration for CI/CD pipelines.

---

**Last Updated**: January 5, 2026
