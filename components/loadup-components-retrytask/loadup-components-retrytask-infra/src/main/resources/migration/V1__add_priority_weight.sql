-- 修复 priority 字段的排序问题
-- 执行日期: 2024-02-11
-- 说明: 将 priority 字段从 varchar 改为 int，使用数值权重替代字符串排序

-- 1. 修改 priority 字段类型为 int，并更新数据
-- 备份旧值: H -> 10, L -> 1
ALTER TABLE `retry_task`
    ADD COLUMN `priority_new` int(11) NOT NULL DEFAULT '1' COMMENT '优先级权重(数值越大优先级越高)' AFTER `priority`;

UPDATE `retry_task` SET `priority_new` = 10 WHERE `priority` = 'H';
UPDATE `retry_task` SET `priority_new` = 1 WHERE `priority` = 'L';

-- 2. 删除旧的 priority 列，重命名新列
ALTER TABLE `retry_task` DROP COLUMN `priority`;
ALTER TABLE `retry_task` CHANGE COLUMN `priority_new` `priority` int(11) NOT NULL DEFAULT '1' COMMENT '优先级权重(数值越大优先级越高: 10=HIGH, 1=LOW)';

-- 3. 添加索引优化查询性能
ALTER TABLE `retry_task` ADD KEY `idx_priority` (`priority`, `next_retry_time`, `status`);

-- 验证迁移
SELECT COUNT(*) as total_count,
       SUM(CASE WHEN priority = 10 THEN 1 ELSE 0 END) as high_priority_count,
       SUM(CASE WHEN priority = 1 THEN 1 ELSE 0 END) as low_priority_count
FROM `retry_task`;

