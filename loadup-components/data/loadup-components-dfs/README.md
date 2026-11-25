# LoadUp DFS

Business code injects `DfsTemplate`. Each binder registers its own implementation.

## Maven

Import the LoadUp BOM, then add `loadup-components-dfs-api` and exactly one binder:

```xml
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-dfs-api</artifactId>
</dependency>
<dependency>
  <groupId>io.github.loadup-cloud</groupId>
  <artifactId>loadup-components-dfs-binder-s3</artifactId>
</dependency>
```

## Configuration

Select a binder by its identifier in `loadup.dfs.binder-type`. The built-in binders use `local`, `s3`, and `database`.
Each additional binder supplies its own identifier. Binder-specific settings live under `loadup.dfs.binder.<type>`.

```yaml
loadup:
  dfs:
    binder-type: s3
    binder.s3.bucket: loadup-files
    binder.s3.region: us-east-1
```

## Capabilities

| Capability                                 | local | s3 | database |
|--------------------------------------------|------:|---:|---------:|
| Upload, download, delete, exists, metadata |    ✓ | ✓ |       ✓ |
| Metadata survives process restart          |    ✓ | ✓ |       ✓ |
| Presigned download URL                     |    ✗ | ✓ |       ✗ |
| Multipart upload                           |    ✗ | ✓ |       ✗ |

S3 is the recommended production binder. Local is for development and single-node deployments; database is a
transitional small-file binder.
