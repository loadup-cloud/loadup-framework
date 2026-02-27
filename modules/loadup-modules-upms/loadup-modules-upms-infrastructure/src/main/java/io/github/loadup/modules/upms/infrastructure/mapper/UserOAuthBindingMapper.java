package io.github.loadup.modules.upms.infrastructure.mapper;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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
import io.github.loadup.modules.upms.infrastructure.dataobject.UserOAuthBindingDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户OAuth绑定 Mapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Mapper
public interface UserOAuthBindingMapper extends BaseMapper<UserOAuthBindingDO> {

    /**
     * 根据提供商和OpenID查询绑定
     */
    @Select("SELECT * FROM upms_user_oauth_binding WHERE provider = #{provider} AND open_id = #{openId} LIMIT 1")
    UserOAuthBindingDO selectByProviderAndOpenId(@Param("provider") String provider, @Param("openId") String openId);

    /**
     * 根据用户ID查询所有绑定
     */
    @Select("SELECT * FROM upms_user_oauth_binding WHERE user_id = #{userId}")
    List<UserOAuthBindingDO> selectByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和提供商查询绑定
     */
    @Select("SELECT * FROM upms_user_oauth_binding WHERE user_id = #{userId} AND provider = #{provider} LIMIT 1")
    UserOAuthBindingDO selectByUserIdAndProvider(@Param("userId") String userId, @Param("provider") String provider);
}

