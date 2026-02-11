package io.github.loadup.retrytask.infra.converter;

import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
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
        if (dbo.getPriority() != null) {
            for (Priority p : Priority.values()) {
                if (p.getCode().equals(dbo.getPriority())) {
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
        if (domain.getPriority() != null) {
            dbo.setPriority(domain.getPriority().getCode());
        }
        dbo.setLastFailureReason(domain.getLastFailureReason());
        dbo.setCreateTime(domain.getCreateTime());
        dbo.setUpdateTime(domain.getUpdateTime());
        return dbo;
    }
}
