package com.github.loadup.components.retrytask.schedule;

/*-
 * #%L
 * loadup-components-retrytask
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.retrytask.config.RetryStrategyConfig;
import com.github.loadup.components.retrytask.config.RetryTaskFactory;
import com.github.loadup.components.retrytask.constant.RetryTaskConstants;
import com.github.loadup.components.retrytask.enums.TaskPriorityEnum;
import com.github.loadup.components.retrytask.model.RetryTask;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * the retry task loader
 */
@Component
@Slf4j
public class RetryTaskLoader {

    /**
     * the repository of retry task
     */
    @Autowired
    private RetryTaskRepository retryTaskRepository;

    /**
     * the manager of retry strategy
     */
    @Autowired
    private RetryTaskFactory retryTaskFactory;

    /**
     * load items
     */
    public List<String> load(String bizType) {

        // SQL执行捞取Task列表
        List<RetryTask> retryTasks = loadTasks(bizType);

        return convertRetryTasks2BusinessKeys(retryTasks);
    }

    /**
     * 生成对应的businessKeys，bizType-bizId
     */
    private List<String> convertRetryTasks2BusinessKeys(List<RetryTask> retryTasks) {

        List<String> businessKeys = new ArrayList<>();
        if (CollectionUtils.isEmpty(retryTasks)) {
            return businessKeys;
        }

        for (RetryTask retryTask : retryTasks) {
            businessKeys.add(retryTask.getBizType() + RetryTaskConstants.INTERVAL_CHAR + retryTask.getBizId());
        }
        return businessKeys;
    }

    /**
     * 捞取TaskId列表
     */
    private List<RetryTask> loadTasks(String bizType) {

        List<RetryTask> resultTasks = new ArrayList<>();

        RetryStrategyConfig retryStrategyConfig = retryTaskFactory.buildRetryStrategyConfig(bizType);

        if (retryStrategyConfig == null) {
            return resultTasks;
        }

        int rowNum = retryStrategyConfig.getMaxLoadNum();

        List<RetryTask> retryTasks = null;
        // 生成虚拟业务id
        if (retryStrategyConfig.isIgnorePriority()) {
            retryTasks = retryTaskRepository.load(bizType, rowNum);
        } else {
            retryTasks = loadByPriority(bizType, rowNum);
        }

        List<RetryTask> unusualRetryTasks =
                retryTaskRepository.loadUnusualTask(bizType, retryStrategyConfig.getExtremeRetryTime(), rowNum);

        if (!CollectionUtils.isEmpty(retryTasks)) {
            resultTasks.addAll(retryTasks);
        }
        if (!CollectionUtils.isEmpty(unusualRetryTasks)) {
            resultTasks.addAll(unusualRetryTasks);
        }

        return resultTasks;
    }

    /**
     * load by priority
     */
    private List<RetryTask> loadByPriority(String bizType, int rowNum) {

        List<RetryTask> retryTasks = new ArrayList<>();

        List<RetryTask> retryTaskHighLevel =
                retryTaskRepository.loadByPriority(bizType, TaskPriorityEnum.H.getCode(), rowNum);

        int remainSize = 0;

        if (CollectionUtils.isEmpty(retryTaskHighLevel)) {
            remainSize = rowNum;
        } else {
            remainSize = rowNum - retryTaskHighLevel.size();
            retryTasks.addAll(retryTaskHighLevel);
        }

        if (remainSize > 0) {
            List<RetryTask> retryTaskLowLevel =
                    retryTaskRepository.loadByPriority(bizType, TaskPriorityEnum.L.getCode(), remainSize);
            if (!CollectionUtils.isEmpty(retryTaskLowLevel)) {
                retryTasks.addAll(retryTaskLowLevel);
            }
        }

        return retryTasks;
    }
}
