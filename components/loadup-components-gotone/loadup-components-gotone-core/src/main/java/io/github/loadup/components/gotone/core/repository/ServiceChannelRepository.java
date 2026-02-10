package io.github.loadup.components.gotone.core.repository;

/*-
 * #%L
 * loadup-components-gotone-core
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

import static io.github.loadup.components.gotone.core.dataobject.table.Tables.SERVICE_CHANNEL_DO;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.gotone.core.dataobject.ServiceChannelDO;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 服务渠道映射 Repository
 */
@Mapper
public interface ServiceChannelRepository extends BaseMapper<ServiceChannelDO> {

    /**
     * 查询启用的渠道配置（按优先级排序）
     */
    default List<ServiceChannelDO> findEnabledChannelsByServiceCode(String serviceCode) {
        QueryWrapper query = QueryWrapper.create()
                .where(SERVICE_CHANNEL_DO.SERVICE_CODE.eq(serviceCode))
                .and(SERVICE_CHANNEL_DO.ENABLED.eq(true))
                .orderBy(SERVICE_CHANNEL_DO.PRIORITY.desc(), SERVICE_CHANNEL_DO.CREATED_AT.asc());
        return selectListByQuery(query);
    }

    /**
     * 查询指定渠道配置
     */
    default Optional<ServiceChannelDO> findByServiceCodeAndChannel(String serviceCode, String channel) {
        QueryWrapper query = QueryWrapper.create()
                .where(SERVICE_CHANNEL_DO.SERVICE_CODE.eq(serviceCode))
                .and(SERVICE_CHANNEL_DO.CHANNEL.eq(channel))
                .and(SERVICE_CHANNEL_DO.ENABLED.eq(true));
        return Optional.ofNullable(selectOneByQuery(query));
    }

    /**
     * 查询所有启用的渠道配置
     */
    default List<ServiceChannelDO> findAllEnabled() {
        QueryWrapper query = QueryWrapper.create()
                .where(SERVICE_CHANNEL_DO.ENABLED.eq(true))
                .orderBy(SERVICE_CHANNEL_DO.PRIORITY.desc());
        return selectListByQuery(query);
    }
}
