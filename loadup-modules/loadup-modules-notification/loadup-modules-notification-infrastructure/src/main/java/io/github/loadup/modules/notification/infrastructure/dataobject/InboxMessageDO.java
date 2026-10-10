/*
 * #%L
 * LoadUp Notification Infrastructure
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
package io.github.loadup.modules.notification.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.modules.notification.domain.model.*;
import java.time.LocalDateTime;

@Table("notification_inbox")
public class InboxMessageDO extends BaseDO {
    private String recipientId;
    private String senderId;
    private String category;
    private String title;
    private String body;
    private String actionUrl;
    private LocalDateTime readAt;
    private String requestKey;
    private LocalDateTime archivedAt;

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String value) {
        this.recipientId = value;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String value) {
        this.senderId = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String value) {
        this.category = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        this.title = value;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String value) {
        this.body = value;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String value) {
        this.actionUrl = value;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime value) {
        this.readAt = value;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String value) {
        this.requestKey = value;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime value) {
        this.archivedAt = value;
    }
}
