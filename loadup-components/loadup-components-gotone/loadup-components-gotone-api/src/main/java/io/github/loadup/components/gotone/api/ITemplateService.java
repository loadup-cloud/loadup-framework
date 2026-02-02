package io.github.loadup.components.gotone.api;

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

import io.github.loadup.components.gotone.model.NotificationTemplate;

/** 模板服务接口 */
public interface ITemplateService {

  /**
   * 渲染模板
   *
   * @param templateCode 模板代码
   * @param params 参数
   * @return 渲染结果
   */
  String render(String templateCode, Object params);

  /**
   * 获取模板
   *
   * @param templateCode 模板代码
   * @return 模板
   */
  NotificationTemplate getTemplate(String templateCode);

  /**
   * 注册模板
   *
   * @param template 模板
   */
  void registerTemplate(NotificationTemplate template);
}
