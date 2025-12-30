package com.github.loadup.components.gotone.converter;

/*-
 * #%L
 * loadup-components-gotone-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loadup.components.gotone.dataobject.ChannelMappingDO;
import com.github.loadup.components.gotone.dataobject.NotificationRecordDO;
import com.github.loadup.components.gotone.domain.ChannelMapping;
import com.github.loadup.components.gotone.domain.NotificationRecord;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

/**
 * Gotone 对象转换器
 */
@Mapper(componentModel = "spring")
public interface GotoneConverter {

    GotoneConverter INSTANCE      = Mappers.getMapper(GotoneConverter.class);
    ObjectMapper    OBJECT_MAPPER = new ObjectMapper();

    // ChannelMapping 转换
    @Mapping(target = "providerList", ignore = true)
    ChannelMapping toChannelMapping(ChannelMappingDO channelMappingDO);

    @Mapping(target = "providerListJson", ignore = true)
    ChannelMappingDO toChannelMappingDO(ChannelMapping channelMapping);

    List<ChannelMapping> toChannelMappingList(List<ChannelMappingDO> channelMappingDOList);

    // NotificationRecord 转换
    @Mapping(target = "receivers", ignore = true)
    NotificationRecord toNotificationRecord(NotificationRecordDO recordDO);

    @Mapping(target = "receiversJson", ignore = true)
    NotificationRecordDO toNotificationRecordDO(NotificationRecord record);

    List<NotificationRecord> toNotificationRecordList(List<NotificationRecordDO> recordDOList);

    // JSON 转换后处理
    @AfterMapping
    default void afterMappingChannelMapping(@MappingTarget ChannelMapping target, ChannelMappingDO source) {
        if (source.getProviderListJson() != null) {
            try {
                target.setProviderList(OBJECT_MAPPER.readValue(
                    source.getProviderListJson(),
                    new TypeReference<List<String>>() {}
                ));
            } catch (JsonProcessingException e) {
                target.setProviderList(new ArrayList<>());
            }
        }
    }

    @AfterMapping
    default void afterMappingChannelMappingDO(@MappingTarget ChannelMappingDO target, ChannelMapping source) {
        if (source.getProviderList() != null) {
            try {
                target.setProviderListJson(OBJECT_MAPPER.writeValueAsString(source.getProviderList()));
            } catch (JsonProcessingException e) {
                target.setProviderListJson("[]");
            }
        }
    }

    @AfterMapping
    default void afterMappingNotificationRecord(@MappingTarget NotificationRecord target, NotificationRecordDO source) {
        if (source.getReceiversJson() != null) {
            try {
                target.setReceivers(OBJECT_MAPPER.readValue(
                    source.getReceiversJson(),
                    new TypeReference<List<String>>() {}
                ));
            } catch (JsonProcessingException e) {
                target.setReceivers(new ArrayList<>());
            }
        }
    }

    @AfterMapping
    default void afterMappingNotificationRecordDO(@MappingTarget NotificationRecordDO target, NotificationRecord source) {
        if (source.getReceivers() != null) {
            try {
                target.setReceiversJson(OBJECT_MAPPER.writeValueAsString(source.getReceivers()));
            } catch (JsonProcessingException e) {
                target.setReceiversJson("[]");
            }
        }
    }
}

