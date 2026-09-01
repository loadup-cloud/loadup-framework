# DFS database binder

Transitional binder for small files when object storage is unavailable. S3 is recommended for production and large files.

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-dfs-binder-database</artifactId>
</dependency>
```

Select it with `loadup.dfs.binder-type=database`. Flyway creates the `dfs_file` table. The binder uses MyBatis-Flex, stores file bytes in a `LONGBLOB`, and persists custom metadata as JSON. It supports the common CRUD and metadata capabilities only; presigned URLs and multipart upload are unsupported.
