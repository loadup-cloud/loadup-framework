package io.github.loadup.components.gotone.model;

/*-
 * #%L
 * loadup-components-gotone-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 渠道发送响应（内部使用）
 */
public class ChannelSendResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 成功数
     */
    private Integer successCount;

    /**
     * 失败数
     */
    private Integer failedCount;

    /**
     * 每个收件人的成功状态
     */
    private Map<String, Boolean> receiverStatus = new HashMap<>();

    /**
     * 每个收件人的错误信息
     */
    private Map<String, String> receiverErrors = new HashMap<>();

    /**
     * 判断指定收件人是否发送成功
     */
    public boolean isSuccess(String receiver) {
        return Boolean.TRUE.equals(receiverStatus.get(receiver));
    }

    /**
     * 获取指定收件人的错误信息
     */
    public String getErrorMessage(String receiver) {
        return receiverErrors.get(receiver);
    }

    public ChannelSendResponse() {}

    public void setContent(String content) {
        this.content = content;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public void setReceiverStatus(Map<String, Boolean> receiverStatus) {
        this.receiverStatus = receiverStatus;
    }

    public void setReceiverErrors(Map<String, String> receiverErrors) {
        this.receiverErrors = receiverErrors;
    }

    public String getContent() {
        return content;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public Map<String, Boolean> getReceiverStatus() {
        return receiverStatus;
    }

    public Map<String, String> getReceiverErrors() {
        return receiverErrors;
    }
}
