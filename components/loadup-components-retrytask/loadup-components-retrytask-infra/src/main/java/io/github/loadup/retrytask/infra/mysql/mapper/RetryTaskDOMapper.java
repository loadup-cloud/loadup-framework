package io.github.loadup.retrytask.infra.mysql.mapper;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.infra.mysql.model.RetryTaskDO;

/**
 * Mapper for converting between {@link RetryTask} and {@link RetryTaskDO}.
 */
public class RetryTaskDOMapper {

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
        domain.setStatus(dbo.getStatus());
        domain.setPriority(dbo.getPriority());
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
        dbo.setStatus(domain.getStatus());
        dbo.setPriority(domain.getPriority());
        dbo.setLastFailureReason(domain.getLastFailureReason());
        dbo.setCreateTime(domain.getCreateTime());
        dbo.setUpdateTime(domain.getUpdateTime());
        return dbo;
    }
}
