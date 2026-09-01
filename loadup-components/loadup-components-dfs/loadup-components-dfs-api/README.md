# DFS API

The facade module defines `DfsService`, `DfsProvider`, immutable file models, and the Spring Boot auto-configuration that wires one provider into `DefaultDfsService`.

Business code should use only `DfsService` and `io.github.loadup.components.dfs.model` types. The API covers object CRUD, metadata, presigned download URLs, and the multipart upload lifecycle. Multipart and presigned operations are optional provider capabilities and report `UnsupportedOperationException` when unavailable.
