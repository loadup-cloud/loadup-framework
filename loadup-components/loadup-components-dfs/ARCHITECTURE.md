# DFS architecture

DFS follows the Mode A contract from `DESIGN.md`:

```text
Business code -> DfsService -> DfsProvider -> one selected binder -> storage system
```

`DfsService` and the immutable models are the facade. `DfsProvider` is the only binder SPI. `DfsAutoConfiguration` creates `DefaultDfsService` only for a single provider, so binder selection is a build/deployment decision rather than runtime routing.

The S3 binder uses AWS SDK `S3Client` and `S3Presigner`, persists user metadata as object metadata, supports presigned GET URLs, and exposes the S3 multipart lifecycle. It accepts AWS S3, MinIO, OSS, COS, and LocalStack through endpoint and path-style settings.

The local binder stores objects and sidecar metadata below the configured root. The database binder stores small objects and JSON metadata in `dfs_file`, using MyBatis-Flex and the standard LoadUp audit columns. Database storage is retained as a transitional implementation and is not the production default.

Download responses implement `AutoCloseable`; callers must close them after consuming the stream. Binders own their clients and never expose SDK types through the facade.
