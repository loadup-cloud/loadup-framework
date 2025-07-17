package com.github.loadup.components.retrytask.notifier.impl;

import com.github.loadup.components.retrytask.model.RetryTaskDO;
import com.github.loadup.components.retrytask.notifier.FailureNotifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpFailureNotifier implements FailureNotifier {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void notify(RetryTaskDO task) {
        if (task.getFailureCallbackUrl() == null) {return;}

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RetryTaskDO> entity = new HttpEntity<>(task, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    task.getFailureCallbackUrl(), entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                // 可记录日志或进行补偿
            }
        } catch (Exception e) {
            // 异常处理
        }
    }
}