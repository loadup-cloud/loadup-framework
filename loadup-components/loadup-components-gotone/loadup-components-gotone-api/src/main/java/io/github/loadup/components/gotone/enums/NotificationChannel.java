package io.github.loadup.components.gotone.enums;

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

/**
 * 通知渠道枚举
 */
public enum NotificationChannel {
    /**
     * 邮件
     */
    EMAIL,

    /**
     * 短信
     */
    SMS,

    /**
     * 推送消息
     */
    PUSH,

    /**
     * Webhook（钉钉、微信、飞书等机器人）
     */
    WEBHOOK,

    /**
     * 站内信
     */
    INTERNAL_MESSAGE,

    /**
     * 微信
     */
    WECHAT,

    /**
     * 钉钉
     */
    DINGTALK,

    /**
     * 飞书
     */
    FEISHU
}
