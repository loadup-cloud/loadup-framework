# DFS tests

The test module verifies the common `DfsService` contract with the local binder and provides integration tests for MySQL and LocalStack.

```bash
mvn test -pl loadup-components/loadup-components-dfs/loadup-components-dfs-test
mvn verify -pl loadup-components/loadup-components-dfs/loadup-components-dfs-test
```

The integration tests use `@EnableTestContainers(ContainerType.MYSQL)` and `@EnableTestContainers(ContainerType.LOCALSTACK)`. Docker is required for the integration phase.
