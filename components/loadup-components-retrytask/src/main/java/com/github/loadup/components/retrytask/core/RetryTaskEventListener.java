//package com.github.loadup.components.retrytask.core;
//
/// *-
// * #%L
// * loadup-components-retrytask
// * %%
// * Copyright (C) 2022 - 2023 loadup_cloud
// * %%
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// * THE SOFTWARE.
// * #L%
// */
//
//import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
//import com.github.loadup.components.retrytask.config.RetryTaskConfigProperties;
//import com.github.loadup.components.retrytask.config.RetryTaskFactory;
//import com.github.loadup.components.retrytask.constant.RetryTaskConstants;
//import com.github.loadup.components.retrytask.enums.DbType;
//import com.github.loadup.components.retrytask.enums.RetryStrategyType;
//import com.github.loadup.components.retrytask.enums.ScheduleExecuteType;
//import com.github.loadup.components.retrytask.enums.SqlType;
//import com.github.loadup.components.retrytask.manager.AsyncTaskStrategyExecutor;
//import com.github.loadup.components.retrytask.manager.DefaultTaskStrategyExecutor;
//import com.github.loadup.components.retrytask.manager.SyncTaskStrategyExecutor;
//import com.github.loadup.components.retrytask.manager.TaskStrategyExecutor;
//import com.github.loadup.components.retrytask.manager.TaskStrategyExecutorFactory;
//import com.github.loadup.components.retrytask.spi.RetryTaskExecutorSpi;
//import com.github.loadup.components.retrytask.strategy.RetryTaskStrategy;
//import com.github.loadup.components.retrytask.strategy.RetryTaskStrategyFactory;
//import com.github.loadup.components.retrytask.utils.ApplicationContextAwareUtil;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import javax.annotation.Resource;
//import javax.sql.DataSource;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.boot.context.event.ApplicationStartedEvent;
//import org.springframework.context.ApplicationListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RetryTaskEventListener implements ApplicationListener<ApplicationStartedEvent> {
//    @Resource
//    private RetryTaskConfigProperties   retryTaskConfigProperties;
//    @Resource
//    private RetryTaskFactory            retryTaskFactory;
//    @Resource
//    private TaskStrategyExecutorFactory taskStrategyExecutorFactory;
//    @Resource
//    private RetryTaskStrategyFactory    retryTaskStrategyFactory;
//    @Resource
//    private DefaultTaskStrategyExecutor defaultTaskStrategyExecutor;
//    @Resource
//    private SyncTaskStrategyExecutor    syncTaskStrategyExecutor;
//    @Resource
//    private AsyncTaskStrategyExecutor   asyncTaskStrategyExecutor;
//    @Resource
//    private DataSource                  dataSource;
//
//    @Override
//    public void onApplicationEvent(ApplicationStartedEvent event) {
//        init();
//    }
//
//    public void init() {
//        //init Strategy
//        initStrategy();
//        //init sql
//        initSqlMap();
//        // init Executor
//        initRetryTaskExecutor();
//        //init wait Strategy
//        initRetryTaskStrategy();
//    }
//
//    private void initStrategy() {
//        Map<String, RetryStrategyConfig> retryStrategyConfigs = retryTaskFactory.getRetryStrategyConfigs();
//        retryTaskConfigProperties.getTaskList().forEach(s -> {
//            if (retryStrategyConfigs.containsKey(s.getBizType())) {
//                throw new RuntimeException("retry strategy exist!bizType=" + s.getBizType());
//            }
//            retryStrategyConfigs.put(s.getBizType(), s);
//        });
//        retryTaskFactory.setRetryStrategyConfigs(retryStrategyConfigs);
//        retryTaskFactory.setDbType(retryTaskConfigProperties.getDbType());
//        retryTaskFactory.setTablePrefix(retryTaskConfigProperties.getTablePrefix());
//
//        Map<String, RetryTaskExecutorSpi> beans = ApplicationContextAwareUtil.getApplicationContext().getBeansOfType(
//                RetryTaskExecutorSpi.class);
//        for (RetryTaskExecutorSpi bean : beans.values()) {
//            String taskType = bean.getTaskType();
//            if (StringUtils.isBlank(taskType)) {
//                throw new RuntimeException(bean.getClass().getName() + " RetryTaskExecutor without taskType");
//            }
//            RetryStrategyConfig retryStrategyConfig = retryTaskFactory.buildRetryStrategyConfig(taskType);
//            retryStrategyConfig.setExecutor(bean);
//        }
//    }
//
//    private void initRetryTaskExecutor() {
//        Map<String, TaskStrategyExecutor> map = new HashMap<>();
//        Map<String, TaskStrategyExecutor> beans = ApplicationContextAwareUtil.getApplicationContext().getBeansOfType(
//                TaskStrategyExecutor.class);
//        for (TaskStrategyExecutor bean : beans.values()) {
//            ScheduleExecuteType executeType = bean.getExecuteType();
//            if (Objects.isNull(executeType)) {
//                throw new RuntimeException(bean.getClass().getName() + " RetryTaskStrategy without executeType");
//            }
//            map.put(executeType.getCode(), bean);
//        }
//        taskStrategyExecutorFactory.setTaskStrategyExecutors(map);
//    }
//
//    private void initRetryTaskStrategy() {
//        Map<String, RetryTaskStrategy> beans = ApplicationContextAwareUtil.getApplicationContext().getBeansOfType(
//                RetryTaskStrategy.class);
//        Map<String, RetryTaskStrategy> map = new HashMap<>();
//        for (RetryTaskStrategy bean : beans.values()) {
//            RetryStrategyType strategyType = bean.getStrategyType();
//            if (Objects.isNull(strategyType)) {
//                throw new RuntimeException(bean.getClass().getName() + " RetryTaskStrategy without strategyType");
//            }
//            map.put(strategyType.getCode(), bean);
//        }
//        retryTaskStrategyFactory.setRetryTaskStrategyMap(map);
//    }
//
//    /**
//     * initate every sql sentence
//     */
//    private void initSqlMap() {
//
//        //initial insert sentence
//        String mysqlOriginalInsertSql =
//                "insert into retry_task (task_id,biz_type,biz_id,executed_times,next_execute_time,max_execute_times,"
//                        + "up_to_max_execute_times_flag,processing_flag,biz_context,gmt_create,gmt_modified,priority,suspended) "
//                        + "values(:taskId,:bizType,:bizId,:executedTimes,:nextExecuteTime,:maxExecuteTimes,:upToMaxExecuteTimesFlag,"
//                        + ":processingFlag,:bizContext,:gmtCreate,:gmtModified,:priority,:suspended)";
//
//        //initial update sentence
//        String mysqlOriginalUpdateSql =
//                "update retry_task set task_id=:taskId,biz_type=:bizType,biz_id=:bizId,executed_times=:executedTimes,"
//                        + "next_execute_time=:nextExecuteTime,max_execute_times=:maxExecuteTimes,"
//                        + "up_to_max_execute_times_flag=:upToMaxExecuteTimesFlag,processing_flag=:processingFlag,biz_context=:bizContext,"
//                        + "gmt_create=:gmtCreate,gmt_modified=:gmtModified,priority=:priority,suspended=:suspended where "
//                        + "task_id=:taskId ";
//
//        //initial load sentence
//        String mysqlOriginalLoadSql =
//                "select task_id,biz_type,biz_id,executed_times,next_execute_time,max_execute_times,up_to_max_execute_times_flag,"
//                        + "processing_flag,biz_context,gmt_create,gmt_modified,priority,suspended"
//                        + " from retry_task where biz_type=:bizType and up_to_max_execute_times_flag='F' and processing_flag='F' and "
//                        + "suspended!='T' and next_execute_time < now() order by priority desc" + " limit :rowNum";
//
//        //initial load by priority sentence
//        String mysqlOriginalLoadByPrioritySql =
//                "select task_id,biz_type,biz_id,executed_times,next_execute_time,max_execute_times,up_to_max_execute_times_flag,"
//                        + "processing_flag,biz_context,gmt_create,gmt_modified,priority,suspended"
//                        + " from retry_task where biz_type=:bizType and up_to_max_execute_times_flag='F' and processing_flag='F' and "
//                        + "suspended!='T' and next_execute_time < now() and priority=:priority " + " limit :rowNum";
//
//        // Unusual Orgi Load Sql
//        String mysqlUnusualOrgiLoadSql =
//                "select task_id,biz_type,biz_id,executed_times,next_execute_time,max_execute_times,up_to_max_execute_times_flag,"
//                        + "processing_flag,biz_context,gmt_create,gmt_modified,priority,suspended"
//                        + " from retry_task where biz_type=:bizType and up_to_max_execute_times_flag='F' and"
//                        + " processing_flag='T' and suspended != 'T' and gmt_modified < DATE_SUB(NOW(), INTERVAL :extremeRetryTime "
//                        + "MINUTE)   limit :rowNum";
//
//        //initial lock sentence
//        String mysqlOriginalLockSql =
//                "select task_id,biz_type,biz_id,executed_times,next_execute_time,max_execute_times,up_to_max_execute_times_flag,"
//                        + "processing_flag,biz_context,gmt_create,gmt_modified,priority,suspended from retry_task where biz_id=:bizId"
//                        + " and biz_type=:bizType for update";
//
//        //initial load by id sentence
//        String mysqlOriginalLoadByIdSql =
//                "select task_id,biz_type,biz_id,executed_times,next_execute_time,max_execute_times,up_to_max_execute_times_flag,"
//                        + "processing_flag,biz_context,gmt_create,gmt_modified,priority,suspended from retry_task where biz_id=:bizId"
//                        + " and biz_type=:bizType";
//
//        //initial delete sentence
//        String originaldeleteSql = "delete from retry_task where biz_id=:bizId and biz_type=:bizType ";
//
//        Map<String, String> sqlMap = new HashMap<>();
//
//        //mysql
//        sqlMap.put(DbType.MYSQL.getCode() + RetryTaskConstants.INTERVAL_CHAR + SqlType.SQL_TASK_INSERT.getCode(),
//                replaceTableName(mysqlOriginalInsertSql, retryTaskConfigProperties.getTablePrefix()));
//        sqlMap.put(DbType.MYSQL.getCode() + RetryTaskConstants.INTERVAL_CHAR + SqlType.SQL_TASK_UPDATE.getCode(),
//                replaceTableName(mysqlOriginalUpdateSql, retryTaskConfigProperties.getTablePrefix()));
//        sqlMap.put(DbType.MYSQL.getCode() + RetryTaskConstants.INTERVAL_CHAR + SqlType.SQL_TASK_LOAD.getCode(),
//                replaceTableName(mysqlOriginalLoadSql, retryTaskConfigProperties.getTablePrefix()));
//        sqlMap.put(DbType.MYSQL.getCode() + RetryTaskConstants.INTERVAL_CHAR + SqlType.SQL_TASK_LOAD_UNUSUAL.getCode(),
//                replaceTableName(mysqlUnusualOrgiLoadSql, retryTaskConfigProperties.getTablePrefix()));
//        sqlMap.put(DbType.MYSQL.getCode() + RetryTaskConstants.INTERVAL_CHAR + SqlType.SQL_TASK_LOCK_BY_ID.getCode(),
//                replaceTableName(mysqlOriginalLockSql, retryTaskConfigProperties.getTablePrefix()));
//        sqlMap.put(DbType.MYSQL.getCode() + RetryTaskConstants.INTERVAL_CHAR + SqlType.SQL_TASK_LOAD_BY_ID.getCode(),
//                replaceTableName(mysqlOriginalLoadByIdSql, retryTaskConfigProperties.getTablePrefix()));
//        sqlMap.put(DbType.MYSQL.getCode() + RetryTaskConstants.INTERVAL_CHAR + SqlType.SQL_TASK_DELETE.getCode(),
//                replaceTableName(originaldeleteSql, retryTaskConfigProperties.getTablePrefix()));
//        sqlMap.put(DbType.MYSQL.getCode() + RetryTaskConstants.INTERVAL_CHAR + SqlType.SQL_TASK_LOAD_BY_PRIORITY.getCode(),
//                replaceTableName(mysqlOriginalLoadByPrioritySql, retryTaskConfigProperties.getTablePrefix()));
//        retryTaskFactory.setSqlMap(sqlMap);
//    }
//
//    private String replaceTableName(String sql, String tablePrefix) {
//        if (StringUtils.isBlank(tablePrefix)) {
//            return sql;
//        }
//        String tableName = tablePrefix + RetryTaskConstants.SUFFIX_TABLE_NAME;
//        return sql.replaceAll(RetryTaskConstants.SUFFIX_TABLE_NAME, tableName);
//    }
//
//}
