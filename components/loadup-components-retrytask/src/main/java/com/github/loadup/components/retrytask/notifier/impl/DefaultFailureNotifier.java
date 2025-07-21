package com.github.loadup.components.retrytask.notifier.impl;

import com.github.loadup.components.retrytask.model.RetryTaskDO;
import com.github.loadup.components.retrytask.notifier.FailureNotifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class DefaultFailureNotifier implements FailureNotifier {

    @Override
    public void notify(RetryTaskDO task) {
        log.info("DefaultFailureNotifier notify task: bizId={},bizType={}", task.getBusinessId(), task.getBusinessType());
    }
}