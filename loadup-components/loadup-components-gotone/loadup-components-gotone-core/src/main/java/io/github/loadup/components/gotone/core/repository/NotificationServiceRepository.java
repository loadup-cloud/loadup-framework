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
import io.github.loadup.components.gotone.core.dataobject.NotificationServiceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 通知服务配置 Repository
 */
@Mapper
public interface NotificationServiceRepository extends BaseMapper<NotificationServiceDO> {

    /**
     * 根据服务代码查询
     */
    @Select("""
        SELECT * FROM gotone_notification_service
        WHERE service_code = #{serviceCode}
        """)
    Optional<NotificationServiceDO> findByServiceCode(String serviceCode);

    /**
     * 根据服务代码查询启用的服务
     */
    @Select("""
        SELECT * FROM gotone_notification_service
        WHERE service_code = #{serviceCode}
        AND enabled = TRUE
        """)
    Optional<NotificationServiceDO> findEnabledByServiceCode(String serviceCode);
}

