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
package io.github.loadup.modules.notification.infrastructure.repository;

import static io.github.loadup.modules.notification.infrastructure.dataobject.table.Tables.*;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.UpdateEntity;
import io.github.loadup.modules.notification.domain.gateway.InboxGateway;
import io.github.loadup.modules.notification.domain.model.*;
import io.github.loadup.modules.notification.infrastructure.converter.NotificationStorageConverter;
import io.github.loadup.modules.notification.infrastructure.dataobject.*;
import io.github.loadup.modules.notification.infrastructure.mapper.*;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.dao.DuplicateKeyException;

public class InboxGatewayImpl implements InboxGateway {

    private final InboxMessageDOMapper mapper;
    private final NotificationStorageConverter converter;

    public InboxGatewayImpl(InboxMessageDOMapper mapper, NotificationStorageConverter converter) {
        this.mapper = mapper;
        this.converter = converter;
    }

    private QueryWrapper scope(String tenant, String recipient) {
        return QueryWrapper.create()
                .from(INBOX_MESSAGE_DO)
                .where(INBOX_MESSAGE_DO.TENANT_ID.eq(tenant))
                .and(INBOX_MESSAGE_DO.RECIPIENT_ID.eq(recipient));
    }

    private QueryWrapper active(String tenant, String recipient) {
        return scope(tenant, recipient).and(INBOX_MESSAGE_DO.ARCHIVED_AT.isNull());
    }

    @Override
    public int insert(InboxMessage message, String requestKey) {
        var entity = converter.toDO(message);
        entity.setRequestKey(requestKey);
        try {
            return mapper.insert(entity);
        } catch (DuplicateKeyException failure) {
            if (requestKey != null
                    && mapper.selectCountByQuery(scope(message.tenantId(), message.recipientId())
                                    .and(INBOX_MESSAGE_DO.REQUEST_KEY.eq(requestKey)))
                            > 0) return 0;
            throw failure;
        }
    }

    @Override
    public InboxPage list(String tenant, String recipient, boolean unread, int page, int size) {
        var q = active(tenant, recipient);
        if (unread) q.and(INBOX_MESSAGE_DO.READ_AT.isNull());
        long total = mapper.selectCountByQuery(q);
        q.orderBy(INBOX_MESSAGE_DO.CREATED_AT.desc(), INBOX_MESSAGE_DO.ID.desc())
                .limit(size)
                .offset((long) (page - 1) * size);
        return new InboxPage(
                mapper.selectListByQuery(q).stream().map(converter::toDomain).toList(), total, page, size);
    }

    @Override
    public long unreadCount(String tenant, String recipient) {
        return mapper.selectCountByQuery(active(tenant, recipient).and(INBOX_MESSAGE_DO.READ_AT.isNull()));
    }

    @Override
    public int markRead(String tenant, String recipient, String id) {
        var patch = UpdateEntity.of(InboxMessageDO.class);
        patch.setReadAt(LocalDateTime.now(java.time.ZoneOffset.UTC));
        int changed = mapper.updateByQuery(
                patch,
                active(tenant, recipient).and(INBOX_MESSAGE_DO.ID.eq(id)).and(INBOX_MESSAGE_DO.READ_AT.isNull()));
        return changed > 0
                ? changed
                : (int) mapper.selectCountByQuery(active(tenant, recipient).and(INBOX_MESSAGE_DO.ID.eq(id)));
    }

    @Override
    public int markAllRead(String tenant, String recipient) {
        var patch = UpdateEntity.of(InboxMessageDO.class);
        patch.setReadAt(LocalDateTime.now(java.time.ZoneOffset.UTC));
        return mapper.updateByQuery(patch, active(tenant, recipient).and(INBOX_MESSAGE_DO.READ_AT.isNull()));
    }

    @Override
    public int archive(String tenant, String recipient, String id) {
        var patch = UpdateEntity.of(InboxMessageDO.class);
        patch.setArchivedAt(LocalDateTime.now(java.time.ZoneOffset.UTC));
        int changed = mapper.updateByQuery(patch, active(tenant, recipient).and(INBOX_MESSAGE_DO.ID.eq(id)));
        return changed > 0
                ? changed
                : (int) mapper.selectCountByQuery(scope(tenant, recipient).and(INBOX_MESSAGE_DO.ID.eq(id)));
    }
}
