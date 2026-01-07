# LoadUp Cache Configuration - New Unified Structure

## Overview

The cache configuration has been optimized to provide a cleaner, more intuitive structure:

- **API Module**: Contains all common cache configurations that work across all implementations
- **Binder Modules**: Contain only implementation-specific settings (Redis connection uses Spring Boot's standard `spring.redis.*`, Caffeine
  uses `spring.cache.caffeine.*`)

## Configuration Structure

### 1. Global Binder Selection

Set the default binder for all caches:

```yaml
loadup:
  cache:
    binder: redis  # or caffeine
```

### 2. Per-Cache Binder Selection

Override the binder for specific caches:

```yaml
loadup:
  cache:
    binder: redis  # Global default
    binders:
      userCache: redis        # Use Redis
      productCache: caffeine  # Use Caffeine (in-memory)
      sessionCache: redis     # Use Redis
```

### 3. Per-Cache Common Configuration

Configure cache-specific properties that work across all implementations:

```yaml
loadup:
  cache:
    userCache:
      maximumSize: 10000           # Max entries (Caffeine only)
      expireAfterWrite: 30m        # TTL after write
      expireAfterAccess: 1h        # TTL after access (Caffeine only)
      enableRandomExpiration: true # Prevent cache avalanche
      randomOffsetSeconds: 300     # Random offset range (0-300s)
      cacheNullValues: true        # Prevent cache penetration
      nullValueExpireAfterWrite: 5m # TTL for null values
      priority: 8                  # Cache priority (1-10)
```

## Implementation-Specific Configuration

### Redis Configuration

Use Spring Boot's standard Redis properties for connection settings:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: yourpassword
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### Caffeine Configuration

Use Spring Boot's standard Caffeine properties for default settings:

```yaml
spring:
  cache:
    caffeine:
      spec: initialCapacity=1000,maximumSize=5000,expireAfterWrite=20m
```

## Complete Examples

### Example 1: Mixed Redis and Caffeine

```yaml
# Spring Boot Redis connection
spring:
  redis:
    host: localhost
    port: 6379
    password: yourpassword

# LoadUp cache configuration
loadup:
  cache:
    binder: redis  # Default to Redis

    # Per-cache binder selection
    binders:
      userCache: redis
      productCache: caffeine  # Use local cache for products
      sessionCache: redis

    # Per-cache configurations
    userCache:
      maximumSize: 10000
      expireAfterWrite: 30m
      enableRandomExpiration: true
      randomOffsetSeconds: 300

    productCache:
      maximumSize: 5000
      expireAfterWrite: 1h
      enableRandomExpiration: true

    sessionCache:
      expireAfterWrite: 24h
      enableRandomExpiration: true
      randomOffsetSeconds: 7200
```

### Example 2: All Caffeine (Development)

```yaml
# Use Caffeine for development
loadup:
  cache:
    binder: caffeine

    userCache:
      maximumSize: 10000
      expireAfterWrite: 30m

    productCache:
      maximumSize: 5000
      expireAfterWrite: 1h
```

### Example 3: All Redis (Production)

```yaml
spring:
  redis:
    host: redis.example.com
    port: 6379
    password: ${REDIS_PASSWORD}

loadup:
  cache:
    binder: redis

    userCache:
      expireAfterWrite: 30m
      cacheNullValues: true

    productCache:
      expireAfterWrite: 1h

    sessionCache:
      expireAfterWrite: 24h
```

## Time Unit Formats

Supported time unit formats for `expireAfterWrite` and `expireAfterAccess`:

- `s` or `S`: seconds (e.g., `300s`)
- `m` or `M`: minutes (e.g., `30m`)
- `h` or `H`: hours (e.g., `2h`)
- `d` or `D`: days (e.g., `1d`)

Examples:

```yaml
expireAfterWrite: 300s  # 300 seconds (5 minutes)
expireAfterWrite: 30m   # 30 minutes
expireAfterWrite: 2h    # 2 hours
expireAfterWrite: 1d    # 1 day
```

## Anti-Cache-Avalanche Strategy

The framework provides built-in protection against cache avalanche:

```yaml
loadup:
  cache:
    userCache:
      expireAfterWrite: 30m         # Base TTL: 30 minutes
      enableRandomExpiration: true  # Enable random offset
      randomOffsetSeconds: 300      # Offset range: 0-300 seconds
```

**Result**: Actual TTL will be: `30m + random(0, 300s)` = between 30-35 minutes

This distributes cache expiration times to prevent all entries from expiring simultaneously.

## Anti-Cache-Penetration Strategy

Prevent cache penetration by caching null values:

```yaml
loadup:
  cache:
    userCache:
      cacheNullValues: true              # Enable null value caching
      nullValueExpireAfterWrite: 5m      # Shorter TTL for null values
```

## Migration from Old Structure

### Old Structure (Deprecated)

```yaml
loadup:
  cache:
    type: redis
    redis:
      cache-config:
        userCache:
          expireAfterWrite: 30m
    caffeine:
      cache-config:
        productCache:
          maximumSize: 5000
```

### New Structure (Recommended)

```yaml
loadup:
  cache:
    binder: redis
    binders:
      productCache: caffeine

    userCache:
      expireAfterWrite: 30m

    productCache:
      maximumSize: 5000
```

## Benefits

1. **Cleaner Structure**: Configuration is flatter and more intuitive
2. **Reuse Spring Boot Standards**: Redis and Caffeine-specific settings use Spring's standard properties
3. **Per-Cache Binder Selection**: Easily mix Redis and Caffeine caches in the same application
4. **Backward Compatible**: Old configuration structure still works with deprecation warnings
5. **Common Properties in API Module**: Shared configuration is centralized in the API module

## Module Organization

```
loadup-components-cache-api/
  ├── CacheProperties.java         # Common cache configuration properties
  ├── LoadUpCacheConfig.java       # Per-cache configuration (all common properties)
  └── CacheAutoConfiguration.java  # Main auto-configuration

loadup-components-cache-binder-redis/
  └── RedisCacheAutoConfiguration.java  # Redis-specific configuration
      # Uses spring.redis.* for connection settings

loadup-components-cache-binder-caffeine/
  └── CaffeineCacheAutoConfiguration.java  # Caffeine-specific configuration
      # Uses spring.cache.caffeine.* for default settings
```

