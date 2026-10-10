# LoadUp Database Processor

Compile-time MyBatis-Flex integration. It generates MyBatis `XxxDOMapper`, `Tables` and TableDef from `@Table` data objects using fixed database conventions.

## Integration

Use `io.github.loadup-cloud:loadup-components-database-processor` as an optional provided dependency and compiler processor path. Follow the complete Maven example in [database README](../README.md#compile-time-generation); select `io.github.loadup.components.database.processor.LoadUpMyBatisFlexProcessor` explicitly alongside any MapStruct/Boot processors.

## Capability matrix

| Capability | Support |
|---|---|
| Generated BaseMapper with MyBatis @Mapper | Yes |
| Module Tables and uppercase TableDef properties | Yes |
| Parent config overrides | Rejected by fixed local defaults |
| MapStruct converters | Separate processor |
| Runtime persistence configuration | Runtime database artifact |
