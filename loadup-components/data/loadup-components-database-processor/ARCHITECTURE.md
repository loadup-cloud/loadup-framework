# Database Processor Architecture

`src/main/resources/io/github/loadup/components/database/processor/mybatis-flex.config` is the canonical immutable configuration. `LoadUpMyBatisFlexProcessor` copies it into compiler output and then delegates to upstream `MybatisFlexProcessor`. `processor.stopBubbling=true` prevents configuration from ancestor directories changing the generated API.

The processor handles only MyBatis-Flex `@Table`; MapStruct and Spring Boot processors run independently. Explicit processor selection prevents the upstream service entry from starting a second generator. The artifact has no Spring runtime dependency and is published separately from the database runtime jar.

The JavaCompiler test compiles a sample DO, generated Mapper and Tables, and verifies that contradictory parent options cannot redirect or disable generation and that the jar resource is copied into compiler output. Integration builds verify real module converters and generated persistence references together. Source output stays in `target/`; clean builds prevent stale generated classes.
