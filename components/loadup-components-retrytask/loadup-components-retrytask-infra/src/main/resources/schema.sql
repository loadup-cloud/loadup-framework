DROP TABLE IF EXISTS `retry_task`;
CREATE TABLE IF NOT EXISTS `retry_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `biz_type` varchar(255) NOT NULL,
  `biz_id` varchar(255) NOT NULL,
  `retry_count` int(11) NOT NULL DEFAULT '0',
  `max_retry_count` int(11) NOT NULL DEFAULT '0',
  `next_retry_time` datetime NOT NULL,
  `status` varchar(255) NOT NULL,
  `priority` char(1) NOT NULL DEFAULT 'L' COMMENT 'Priority: H for High, L for Low',
  `last_failure_reason` text,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_type_biz_id` (`biz_type`,`biz_id`),
  KEY `idx_status_priority_time` (`status`, `priority`, `next_retry_time`)
);
