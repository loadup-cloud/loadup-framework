package com.github.loadup.components.retrytask.repository;

import com.github.loadup.components.retrytask.model.RetryTask;

import java.util.List;

/**
 * @author lise
 * @since 1.0.0
 */
public interface RetryTaskRepositoryExt {

    /**
     * lock retry task by Id
     */
    RetryTask lockByBizId(String bizId, String bizType);

    /**
     * load retry task by Id
     */
    RetryTask findByBizId(String bizId, String bizType);

    /**
     * delete the task
     */
    void delete(String bizId, String bizType);

    /**
     * 根据bizType和规定最大捞取时间捞取rowNum数量的任务id
     * 捞取没有超过次数限制而且未执行过的流水
     */
    List<RetryTask> load(String bizType, int rowNum);

    /**
     * 根据bizType和规定最大捞取时间捞取rowNum数量的任务id(不区分租户id)，只能给到调度三层分发的Loader层使用
     * <p>
     * 捞取没有超过次数限制而且未执行过的流水,根据优先级进行排序
     */
    List<RetryTask> loadByPriority(String bizType, String priority, int rowNum);

    /**
     * 根据bizType和规定最大捞取时间捞取rowNum数量的任务id(不区分租户id)，只能给到调度三层分发的Loader层使用
     * <p>
     * 捞取没有超过次数限制而且状态为已执行过但是超过30分钟还没有被删除的流水
     */
    List<RetryTask> loadUnusualTask(String bizType, int extremeRetryTime, int rowNum);
}
