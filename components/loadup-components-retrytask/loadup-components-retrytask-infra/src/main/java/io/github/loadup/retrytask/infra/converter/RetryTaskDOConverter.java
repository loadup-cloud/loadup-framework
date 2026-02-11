package io.github.loadup.retrytask.infra.converter;

/*-
 * #%L
 * Loadup Components Retrytask Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.infra.model.RetryTaskDO;

/**
 * Mapper for converting between {@link RetryTask} and {@link RetryTaskDO}.
 */
public class RetryTaskDOConverter {

    public static RetryTask toDomain(RetryTaskDO dbo) {
        if (dbo == null) {
            return null;
        }
        RetryTask domain = new RetryTask();
        domain.setId(dbo.getId());
        domain.setBizType(dbo.getBizType());
        domain.setBizId(dbo.getBizId());
        domain.setRetryCount(dbo.getRetryCount());
        domain.setMaxRetryCount(dbo.getMaxRetryCount());
        domain.setNextRetryTime(dbo.getNextRetryTime());
        if (dbo.getStatus() != null) {
            try {
                domain.setStatus(RetryTaskStatus.valueOf(dbo.getStatus()));
            } catch (IllegalArgumentException e) {
                // handle unknown status or log warning
            }
        }
        // 根据 priority weight 转换为枚举
        if (dbo.getPriority() != null) {
            for (Priority p : Priority.values()) {
                if (p.getWeight() == dbo.getPriority()) {
                    domain.setPriority(p);
                    break;
                }
            }
        }
        domain.setLastFailureReason(dbo.getLastFailureReason());
        domain.setCreateTime(dbo.getCreateTime());
        domain.setUpdateTime(dbo.getUpdateTime());
        return domain;
    }

    public static RetryTaskDO toDbo(RetryTask domain) {
        if (domain == null) {
            return null;
        }
        RetryTaskDO dbo = new RetryTaskDO();
        dbo.setId(domain.getId());
        dbo.setBizType(domain.getBizType());
        dbo.setBizId(domain.getBizId());
        dbo.setRetryCount(domain.getRetryCount());
        dbo.setMaxRetryCount(domain.getMaxRetryCount());
        dbo.setNextRetryTime(domain.getNextRetryTime());
        if (domain.getStatus() != null) {
            dbo.setStatus(domain.getStatus().name());
        }
        // priority 字段直接存储 weight 数值
        if (domain.getPriority() != null) {
            dbo.setPriority(domain.getPriority().getWeight());
        } else {
            dbo.setPriority(Priority.LOW.getWeight()); // 默认低优先级
        }
        dbo.setLastFailureReason(domain.getLastFailureReason());
        dbo.setCreateTime(domain.getCreateTime());
        dbo.setUpdateTime(domain.getUpdateTime());
        return dbo;
    }
}
