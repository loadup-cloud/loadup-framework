# Testify Demo Module

This module demonstrates the usage of the Testify framework with example services and test cases.

## Overview

The demo shows how to:

- Define test data using variables (Faker, time functions, custom functions)
- Write database assertions with operators
- Use YAML-driven tests with zero code intrusion

## Structure

```
testify-demo/
├── src/main/java/
│   └── com/github/loadup/testify/demo/
│       ├── model/         # Data models (User, Order, etc.)
│       └── service/       # Business services
│
└── src/test/
    ├── java/
    │   └── com/github/loadup/testify/demo/
    │       └── service/   # Test classes extending TestifyBase
    │
    └── resources/
        ├── application-test.yml  # Test configuration
        └── testcases/            # YAML test definitions
            └── [TestClass]/
                └── [testMethod].yaml
```

## Quick Start

### 1. Build the Framework

```bash
cd /Users/lise/PersonalSpace/loadup-cloud/loadup-testify
mvn clean install -DskipTests
```

### 2. Run Tests

```bash
cd testify-demo
mvn test
```

## Example Usage

### Service Class (No Changes Required!)

```java

@Service
public class UserService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createUser(String userId, String userName, String email) {
        jdbcTemplate.update(
                "INSERT INTO users (user_id, user_name, email, status) VALUES (?, ?, ?, ?)",
                userId, userName, email, "ACTIVE"
        );
    }
}
```

### Test Class

```java
public class UserServiceTest extends TestifyBase {

    @Autowired
    private UserService userService;

    @Test
    public void testCreateUser() {
        // Test is driven by YAML file:
        // src/test/resources/testcases/UserServiceTest/testCreateUser.yaml
        // Framework automatically:
        // 1. Loads YAML and resolves variables
        // 2. Executes cleanup SQL
        // 3. Calls service method with input params
        // 4. Asserts database state
    }
}
```

### YAML Test Definition

```yaml
variables:
  userId: ${fn.uuid()}
  userName: ${faker.name.fullName()}
  email: ${faker.internet.emailAddress()}
  createdAt: ${time.now()}

input:
  - ${userId}
  - ${userName}
  - ${email}

setup:
  clean_sql: DELETE FROM users WHERE user_id = '${userId}'

expect:
  database:
    table: users
    mode: strict
    rows:
      - _match: { user_id: ${ userId } }
        user_name: ${userName}
        email: ${email}
        status: ACTIVE
        created_at:
          op: approx
          val: ${createdAt}
          delta: 1000
```

## Current Status

> **Note**: This demo is a work in progress. The following components are partially implemented:

- ✅ Framework core (data engine, assert engine, mock engine)
- ✅ SQL Executor
- ✅ TestifyBase helper class
- 🚧 TestNG Listener integration (to be completed)
- 🚧 Data Provider (to be completed)
- 🚧 Sample services and tests (to be completed)

Once the TestNG integration is complete, tests can be run with full YAML-driven automation.

## Configuration

### application-test.yml

```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    username: sa
    password:
    
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql

testify:
  containers:
    enabled: false  # Using H2 in-memory database
  database:
    columnNamingStrategy: caseInsensitive
```

## Features Demonstrated

### Variable Types

- **Faker**: `${faker.name.fullName()}`, `${faker.internet.emailAddress()}`
- **Time**: `${time.now()}`, `${time.now('+1d')}`, `${time.epochMilli()}`
- **Functions**: `${fn.uuid()}`, `${fn.random(1, 100)}`
- **References**: `${userId}`, `${userName}`

### Operators

- `eq`: Exact match
- `ne`: Not equal
- `gt`, `ge`, `lt`, `le`: Numeric comparisons
- `regex`: Pattern matching
- `approx`: Time approximation
- `json`: JSON comparison (partial/full)
- `contains`: String contains

### Database Features

- Case-insensitive column matching
- Row matching by `_match` criteria
- Rich diff reporting with ASCII tables
- Variable substitution in SQL

## Next Steps

To complete the demo:

1. Implement TestNG Listener for automatic YAML loading
2. Implement Data Provider for parameter injection
3. Create sample services (UserService, OrderService)
4. Write comprehensive YAML test cases
5. Add schema.sql for H2 database initialization
