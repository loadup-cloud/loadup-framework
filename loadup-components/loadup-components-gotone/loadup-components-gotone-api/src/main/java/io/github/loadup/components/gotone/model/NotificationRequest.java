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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 通知发送请求（ServiceCode驱动架构）
 *
 * <p>通过 serviceCode 驱动通知发送，后台自动根据配置路由到对应渠道。
 * <p>业务代码与具体渠道解耦，支持动态启用/禁用渠道。
 */
public class NotificationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务代码（必填）
     * <p>用于查询通知服务配置，确定发送渠道和模板
     * <p>示例：ORDER_CREATED, USER_REGISTERED, PASSWORD_RESET
     */
    @NotBlank(message = "服务代码不能为空")
    private String serviceCode;

    /**
     * 收件人列表（必填）
     * <p>格式根据渠道自动识别：
     * <ul>
     *   <li>邮箱格式 → EMAIL</li>
     *   <li>手机号格式 → SMS</li>
     *   <li>其他 → PUSH（设备Token或用户ID）</li>
     * </ul>
     */
    @NotEmpty(message = "收件人列表不能为空")
    private List<String> receivers;

    /**
     * 模板参数（必填）
     * <p>用于模板内容渲染，支持 ${varName} 占位符
     * <p>示例：{"userName": "张三", "orderNo": "12345", "amount": "99.00"}
     */
    @NotNull(message = "模板参数不能为空")
    private Map<String, Object> templateParams;

    /**
     * 请求ID（可选，用于幂等性）
     * <p>同一个 requestId 只会发送一次
     * <p>建议使用业务唯一标识，如：ORDER_{orderId}
     */
    private String requestId;

    /**
     * 指定渠道（可选）
     * <p>如果不指定，则根据 serviceCode 配置的所有渠道发送
     * <p>如果指定，则只发送指定渠道
     * <p>示例：["EMAIL", "SMS"]
     */
    private List<String> channels;

    /**
     * 是否异步发送（可选）
     * <p>如果不指定，则根据渠道配置的 sendStrategy 决定
     * <p>true = 异步发送，false = 同步发送
     */
    private Boolean async;

    public NotificationRequest(
            String serviceCode,
            List<String> receivers,
            Map<String, Object> templateParams,
            String requestId,
            List<String> channels,
            Boolean async) {
        this.serviceCode = serviceCode;
        this.receivers = receivers;
        this.templateParams = templateParams;
        this.requestId = requestId;
        this.channels = channels;
        this.async = async;
    }

    public NotificationRequest() {}

    public String getServiceCode() {
        return this.serviceCode;
    }

    public List<String> getReceivers() {
        return this.receivers;
    }

    public Map<String, Object> getTemplateParams() {
        return this.templateParams;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public List<String> getChannels() {
        return this.channels;
    }

    public Boolean isAsync() {
        return this.async;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public void setTemplateParams(Map<String, Object> templateParams) {
        this.templateParams = templateParams;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String serviceCode;
        private List<String> receivers;
        private Map<String, Object> templateParams;
        private String requestId;
        private List<String> channels;
        private Boolean async;

        public Builder serviceCode(String serviceCode) {
            this.serviceCode = serviceCode;
            return this;
        }

        public Builder receivers(List<String> receivers) {
            this.receivers = receivers;
            return this;
        }

        public Builder templateParams(Map<String, Object> templateParams) {
            this.templateParams = templateParams;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder channels(List<String> channels) {
            this.channels = channels;
            return this;
        }

        public Builder async(Boolean async) {
            this.async = async;
            return this;
        }

        public NotificationRequest build() {
            return new NotificationRequest(
                    this.serviceCode, this.receivers, this.templateParams, this.requestId, this.channels, this.async);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
