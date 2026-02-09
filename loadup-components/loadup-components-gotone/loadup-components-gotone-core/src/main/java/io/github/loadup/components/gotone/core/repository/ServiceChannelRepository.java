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

import com.mybatisflex.core.BaseMapper;
import io.github.loadup.components.gotone.core.dataobject.ServiceChannelDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

/**
 * 服务渠道映射 Repository
 */
@Mapper
public interface ServiceChannelRepository extends BaseMapper<ServiceChannelDO> {

    /**
     * 查询启用的渠道配置（按优先级排序）
     */
    @Select("""
        SELECT * FROM gotone_service_channel
        WHERE service_code = #{serviceCode}
        AND enabled = TRUE
        ORDER BY priority DESC, created_at ASC
        """)
    List<ServiceChannelDO> findEnabledChannelsByServiceCode(String serviceCode);

    /**
     * 查询指定渠道配置
     */
    @Select("""
        SELECT * FROM gotone_service_channel
        WHERE service_code = #{serviceCode}
        AND channel = #{channel}
        AND enabled = TRUE
        """)
    Optional<ServiceChannelDO> findByServiceCodeAndChannel(String serviceCode, String channel);

    /**
     * 查询所有启用的渠道配置
     */
    @Select("""
        SELECT * FROM gotone_service_channel
        WHERE enabled = TRUE
        ORDER BY priority DESC
        """)
    List<ServiceChannelDO> findAllEnabled();
}

