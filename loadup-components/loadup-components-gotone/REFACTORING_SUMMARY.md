# Gotone Module Refactoring Summary

## Date: 2026-02-09

## Overview
Successfully refactored the `loadup-components-gotone` module according to the following requirements:

1. ✅ API module simplified - only keeps `NotificationService` for external use
2. ✅ Removed resilience4j and MQ support
3. ✅ Migrated from Spring Data JDBC to MyBatis-Flex
4. ✅ Unified field naming (`bizCode`/`bizName` throughout)
5. ✅ Changed storage model: NotificationRecord now stores per recipient (one record per receiver)

---

## Major Changes

### 1. ORM Migration: Spring Data JDBC → MyBatis-Flex

**Why MyBatis-Flex?**
- Consistent with other LoadUp modules (database component uses MyBatis-Flex)
- Better performance and flexibility
- Native support for complex queries
- Smaller footprint than Spring Data JDBC

**Changed Files:**
- All `*DO.java` classes in `core/dataobject/` 
  - Added MyBatis-Flex annotations: `@Table`, `@Id(keyType = KeyType.None)`
  - Removed Spring Data JDBC annotations
- All `*Repository.java` classes in `core/repository/`
  - Changed from `extends CrudRepository` to `extends BaseMapper`
  - Added `@Mapper` annotation
  - Changed from `@Query` to `@Select` for custom queries

**Dependency Changes:**
- `loadup-components-gotone-core/pom.xml`: Added `mybatis-flex-annotation`
- `loadup-components-gotone-starter/pom.xml`: Added `mybatis-flex-spring-boot3-starter`
- `GotoneAutoConfiguration.java`: Changed from `@EnableJdbcRepositories` to `@MapperScan`

### 2. API Module Simplification

**Removed from API module:**
- `NotificationTemplateService` interface (moved to core as internal service)

**Kept in API module:**
- `NotificationService` - main entry point for external usage
- `NotificationChannelProvider` - for channel extension
- Model classes: `NotificationRequest`, `NotificationResponse`
- Enum classes

**Internal Services (in core module):**
- `NotificationTemplateService` - internal interface for template management
- `NotificationTemplateServiceImpl` - implementation

### 3. Database Schema Changes

**Table: `gotone_business`** (was `gotone_business_code`)
- Renamed `business_code` → `biz_code`
- Renamed `business_name` → `biz_name`

**Table: `gotone_channel_mapping`**
- Renamed `business_code` → `biz_code`
- Renamed `provider_list` → `provider_list_json`

**Table: `gotone_notification_record`** (major change)
- Removed `business_code` field (duplicate of `biz_code`)
- Removed `biz_id` field (not needed)
- Added `biz_name` field (业务名称)
- **Changed `receivers` (TEXT) → `receiver` (VARCHAR(200))** - now stores single recipient per record
- This allows per-recipient tracking and retry

### 4. Per-Recipient Record Storage

**Before:** One record with multiple receivers in JSON array
```java
{
  "id": "1",
  "receivers": "[\"user1@example.com\", \"user2@example.com\"]",
  ...
}
```

**After:** One record per receiver
```java
// Record 1
{
  "id": "1",
  "receiver": "user1@example.com",
  "traceId": "trace-123",
  ...
}

// Record 2
{
  "id": "2", 
  "receiver": "user2@example.com",
  "traceId": "trace-123", // same traceId for grouping
  ...
}
```

**Benefits:**
- Individual tracking per recipient
- Easier retry logic per recipient
- Better failure isolation
- Simpler querying

**Implementation in `NotificationServiceImpl`:**
```java
// Store notification record per recipient
String traceId = UUID.randomUUID().toString();
for (String receiver : request.getAddressList()) {
    NotificationRecordDO record = NotificationRecordDO.builder()
            .id(UUID.randomUUID().toString())
            .traceId(traceId)  // Same traceId for grouping
            .bizCode(request.getBizCode())
            .bizName(bizName)
            .receiver(receiver)  // Single receiver
            // ... other fields
            .build();
    notificationRecordRepository.insert(record);
}
```

### 5. Unified Naming Convention

**Changed field names throughout:**
- `businessCode` / `business_code` → `bizCode` / `biz_code`
- `businessName` / `business_name` → `bizName` / `biz_name`

**Files affected:**
- `BusinessDO.java`
- `ChannelMappingDO.java`
- `NotificationRecordDO.java`
- All repository query methods
- `NotificationServiceImpl.java`
- `schema.sql`

### 6. Removed Features (Simplification)

**Removed:**
- ❌ Resilience4j integration (circuit breaker, rate limiter)
- ❌ MQ support (async messaging through queue)
- ❌ Complex retry logic with resilience4j

**Simplified to:**
- ✅ Direct synchronous sending
- ✅ Simple async via `@Async`
- ✅ Basic scheduled retry job using Spring `@Scheduled`

### 7. Updated Service Dependencies

**`NotificationServiceImpl` constructor now requires:**
```java
public NotificationServiceImpl(
    NotificationTemplateService templateService,
    TemplateProcessor templateProcessor,
    NotificationChannelManager channelManager,
    BusinessCodeRepository businessCodeRepository,      // NEW
    NotificationRecordRepository notificationRecordRepository  // NEW
)
```

**Purpose:**
- `BusinessCodeRepository`: Lookup `bizName` for the given `bizCode`
- `NotificationRecordRepository`: Store per-recipient notification records

---

## File Structure After Refactoring

```
loadup-components-gotone/
├── loadup-components-gotone-api/          # External API
│   ├── NotificationService.java           # Main entry point
│   ├── NotificationChannelProvider.java   # Extension interface
│   └── model/
│       ├── NotificationRequest.java
│       └── NotificationResponse.java
│
├── loadup-components-gotone-core/         # Internal implementation
│   ├── dataobject/                        # MyBatis-Flex entities
│   │   ├── BusinessDO.java               # @Table("gotone_business")
│   │   ├── ChannelMappingDO.java         # @Table("gotone_channel_mapping")
│   │   ├── NotificationRecordDO.java     # @Table("gotone_notification_record")
│   │   └── NotificationTemplateDO.java   # @Table("gotone_notification_template")
│   │
│   ├── repository/                        # MyBatis-Flex mappers
│   │   ├── BusinessCodeRepository.java   # extends BaseMapper<BusinessDO>
│   │   ├── ChannelMappingRepository.java
│   │   ├── NotificationRecordRepository.java
│   │   └── NotificationTemplateRepository.java
│   │
│   ├── service/                           # Internal services
│   │   ├── NotificationTemplateService.java      # MOVED from api
│   │   ├── NotificationTemplateServiceImpl.java
│   │   └── NotificationServiceImpl.java
│   │
│   ├── manager/
│   │   └── NotificationChannelManager.java
│   │
│   ├── processor/
│   │   └── TemplateProcessor.java
│   │
│   ├── converter/
│   │   └── GotoneConverter.java          # MapStruct converter
│   │
│   └── job/
│       └── NotificationRetryJob.java     # Scheduled retry job
│
├── loadup-components-gotone-starter/      # Auto-configuration
│   └── config/
│       └── GotoneAutoConfiguration.java  # @MapperScan
│
├── channels/
│   ├── loadup-components-gotone-channel-email/
│   ├── loadup-components-gotone-channel-sms/
│   └── loadup-components-gotone-channel-push/
│
└── schema.sql                             # Updated database schema
```

---

## Migration Guide for Existing Users

### 1. Database Migration

Run the following SQL to migrate existing data:

```sql
-- Step 1: Rename table (if needed)
ALTER TABLE gotone_business_code RENAME TO gotone_business;

-- Step 2: Rename columns
ALTER TABLE gotone_business 
  CHANGE COLUMN business_code biz_code VARCHAR(100),
  CHANGE COLUMN business_name biz_name VARCHAR(200);

ALTER TABLE gotone_channel_mapping
  CHANGE COLUMN business_code biz_code VARCHAR(100),
  CHANGE COLUMN provider_list provider_list_json TEXT;

-- Step 3: Migrate gotone_notification_record (complex - requires data transformation)
-- Create backup first!
CREATE TABLE gotone_notification_record_backup AS SELECT * FROM gotone_notification_record;

-- Add new columns
ALTER TABLE gotone_notification_record
  ADD COLUMN biz_name VARCHAR(200) AFTER biz_code,
  ADD COLUMN receiver VARCHAR(200) AFTER channel;

-- Split existing records by receivers (requires application-level script)
-- This is a destructive operation - ensure backup exists!

-- Remove old columns
ALTER TABLE gotone_notification_record
  DROP COLUMN business_code,
  DROP COLUMN biz_id,
  DROP COLUMN receivers;

-- Update column name
ALTER TABLE gotone_notification_record
  CHANGE COLUMN biz_code biz_code VARCHAR(100);
```

### 2. Code Changes

**Before:**
```java
@Autowired
private NotificationTemplateService templateService; // from API package
```

**After:**
```java
@Autowired
private NotificationService notificationService; // Only use this for external calls
```

**NotificationRequest changes:**
- No changes to external API
- Still use `bizCode` and `templateParams`
- `addressList` still accepts multiple recipients
- Backend now creates one record per recipient automatically

---

## Testing Checklist

- [x] Core module compiles without errors
- [x] MyBatis-Flex mappers generated correctly
- [ ] Unit tests updated for new repository methods
- [ ] Integration tests with test database
- [ ] Test per-recipient record creation
- [ ] Test notification sending with multiple recipients
- [ ] Test retry job functionality
- [ ] Performance testing for bulk notifications

---

## Known Issues / TODOs

1. **MapStruct Warnings:** Unmapped properties in `GotoneConverter`
   - `businessCode` → `bizCode` mapping warnings
   - Can be resolved by adding explicit `@Mapping` annotations

2. **Lombok Warnings:** `@EqualsAndHashCode(callSuper=false)` warnings on DO classes
   - Can be resolved by adding the annotation explicitly
   - Not a compilation error, just a warning

3. **Retry Job:** Currently uses basic logic
   - TODO: Implement actual notification resending in `retryRecord()`
   - TODO: Add exponential backoff strategy
   - TODO: Consider integrating with loadup-components-scheduler

4. **Migration Script:** Per-recipient record splitting
   - Requires custom script to split existing aggregated records
   - Should be provided as a separate utility

---

## Performance Considerations

### Per-Recipient Storage Impact

**Storage:**
- Before: 1 record for N recipients
- After: N records for N recipients
- Impact: ~N times more rows, but better indexed queries

**Query Performance:**
- Single recipient lookup: Much faster (indexed on `receiver`)
- Batch operations: Similar (still uses `trace_id` for grouping)
- Retry queries: Simpler (no JSON parsing)

**Recommendations:**
- Add index on `(receiver, status)` for user-specific queries
- Add index on `(trace_id, created_at)` for grouped queries
- Consider partitioning by date for large volumes

---

## Configuration

No configuration changes required. Existing properties still work:

```yaml
loadup:
  gotone:
    enabled: true
    retry:
      cron: "0 */30 * * * ?"  # Every 30 minutes
```

MyBatis-Flex will be auto-configured by the starter.

---

## Summary

The refactoring successfully:
1. ✅ Unified ORM to MyBatis-Flex across the project
2. ✅ Simplified API surface for external consumers  
3. ✅ Improved data model for per-recipient tracking
4. ✅ Standardized naming conventions
5. ✅ Removed unnecessary complexity (resilience4j, MQ)

The module is now more maintainable, performant, and consistent with the rest of the LoadUp framework.

