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

import static io.github.loadup.components.gotone.core.dataobject.table.Tables.NOTIFICATION_SERVICE_DO;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.loadup.components.gotone.core.dataobject.NotificationServiceDO;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知服务配置 Repository
 */
@Mapper
public interface NotificationServiceRepository extends BaseMapper<NotificationServiceDO> {

    /**
     * 根据服务代码查询
     */
    default Optional<NotificationServiceDO> findByServiceCode(String serviceCode) {
        QueryWrapper query = QueryWrapper.create().where(NOTIFICATION_SERVICE_DO.SERVICE_CODE.eq(serviceCode));
        return Optional.ofNullable(selectOneByQuery(query));
    }

    /**
     * 根据服务代码查询启用的服务
     */
    default Optional<NotificationServiceDO> findEnabledByServiceCode(String serviceCode) {
        QueryWrapper query = QueryWrapper.create()
                .where(NOTIFICATION_SERVICE_DO.SERVICE_CODE.eq(serviceCode))
                .and(NOTIFICATION_SERVICE_DO.ENABLED.eq(true));
        return Optional.ofNullable(selectOneByQuery(query));
    }
}
