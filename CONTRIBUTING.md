# Contributing

## Build

Use Java 21 and Maven 3.6 or newer.

```bash
mvn clean verify
mvn spotless:apply
```

## Project rules

- Keep dependencies flowing from commons to components to modules to the application.
- Use constructor injection and explicit Java code; do not use Lombok or field injection.
- Do not add controllers. Expose module operations through configured Gateway bean routes.
- Keep domain modules free of Spring and ORM annotations.
- Use MyBatis-Flex `QueryWrapper`; do not concatenate SQL.
- Extend `BaseDO` for persistent objects and do not redeclare its standard fields.
- Use `VARCHAR(64)` IDs and `TINYINT` flags. Manage dependency versions in the BOM.
- Write Java comments and Javadoc in English.
- Add focused tests and update the module README or ARCHITECTURE when behavior changes.
