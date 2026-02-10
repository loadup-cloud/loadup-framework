drop table if exists "retry_task";
CREATE TABLE "retry_task" (
    "id" bigint NOT NULL AUTO_INCREMENT,
    "biz_type" varchar(255) NOT NULL,
    "biz_id" varchar(255) NOT NULL,
    "retry_count" int NOT NULL DEFAULT 0,
    "max_retry_count" int NOT NULL DEFAULT 0,
    "next_retry_time" datetime NOT NULL,
    "status" varchar(255) NOT NULL,
    "priority" char(1) NOT NULL DEFAULT 'L',
    "last_failure_reason" text,
    "create_time" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time" datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id"),
    UNIQUE ("biz_type", "biz_id")
);