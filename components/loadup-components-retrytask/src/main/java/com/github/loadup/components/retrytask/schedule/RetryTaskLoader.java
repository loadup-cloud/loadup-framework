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
import com.github.loadup.components.retrytask.constant.RetryTaskConstants;
import com.github.loadup.components.retrytask.enums.TaskPriorityEnum;
import com.github.loadup.components.retrytask.factory.RetryStrategyFactory;
import com.github.loadup.components.retrytask.model.RetryTaskDO;
import com.github.loadup.components.retrytask.repository.RetryTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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
    private RetryStrategyFactory retryTaskFactory;

    /**
     * load items
     */
    public List<String> load(String bizType) {

        // SQL执行捞取Task列表
        List<RetryTaskDO> retryTasks = loadTasks(bizType);

        return convertRetryTasks2BusinessKeys(retryTasks);
    }

    /**
     * 生成对应的businessKeys，bizType-bizId
     */
    private List<String> convertRetryTasks2BusinessKeys(List<RetryTaskDO> retryTasks) {

        if (CollectionUtils.isEmpty(retryTasks)) {
            return Collections.EMPTY_LIST;
        }

        List<String> businessKeys = retryTasks.stream().map(
                retryTask -> retryTask.getBusinessType() + RetryTaskConstants.INTERVAL_CHAR + retryTask.getBusinessId()).collect(
                Collectors.toList());
        return businessKeys;
    }

    /**
     * 捞取TaskId列表
     */
    public List<RetryTaskDO> loadTasks(String bizType) {

        List<RetryTaskDO> resultTasks = new ArrayList<>();

        RetryStrategyConfig retryStrategyConfig = retryTaskFactory.getStrategyConfig(bizType);

        if (retryStrategyConfig == null) {
            return resultTasks;
        }

        int rowNum = retryStrategyConfig.getMaxLoadNum();

        // First load high priority tasks
        List<RetryTaskDO> highPriorityTasks = loadByPriority(bizType, TaskPriorityEnum.HIGH, rowNum);
        resultTasks.addAll(highPriorityTasks);
        int remaining = rowNum - resultTasks.size();
        // Then load low priority tasks if needed
        if (remaining > 0) {
            List<RetryTaskDO> lowPriorityTasks = loadByPriority(bizType, TaskPriorityEnum.LOW, remaining);
            resultTasks.addAll(lowPriorityTasks);
        }

        //load exception tasks
        List<RetryTaskDO> unusualRetryTasks =
                retryTaskRepository.loadUnusualTask(bizType, retryStrategyConfig.getExtremeRetryTime(), rowNum);
        if (!CollectionUtils.isEmpty(unusualRetryTasks)) {
            resultTasks.addAll(unusualRetryTasks);
        }

        return resultTasks;
    }

    /**
     * load by priority
     */
    private List<RetryTaskDO> loadByPriority(String bizType, TaskPriorityEnum priority, int rowNum) {
        List<RetryTaskDO> tasks = retryTaskRepository.loadByPriority(bizType, priority.getCode(), rowNum);
        return tasks != null ? tasks : new ArrayList<>();
    }
}
