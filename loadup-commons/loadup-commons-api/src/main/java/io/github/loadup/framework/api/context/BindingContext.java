package io.github.loadup.framework.api.context;

/*-
 * #%L
 * Loadup Common Api
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

import io.github.loadup.framework.api.binder.Binder;
import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import java.util.Collections;
import java.util.List;
import org.springframework.context.ApplicationContext;

/** 绑定上下文：包含创建 Binding 所需的所有原材料 */
public class BindingContext<B extends Binder, C extends BaseBindingCfg> {
  /** 业务标识 (对应 YAML 中的 bindings 节点 key，如 userCache) */
  private final String bizTag;

  /** 驱动类型字符串 (对应 YAML 中的 binder-type，如 "caffeine,redis") */
  private final String binderType;

  /** 已经解析完成并完成属性绑定的业务配置对象 */
  private final C bindingCfg;

  /** 已经实例化并注入了配置的驱动列表 (有序，支持多级缓存) */
  private final List<B> binders;

  /** Spring 应用上下文，方便 Binding 内部手动获取其他 Bean */
  private final ApplicationContext applicationContext;

  public BindingContext(
      String bizTag,
      String binderType,
      C bindingCfg,
      List<B> binders,
      ApplicationContext applicationContext) {
    this.bizTag = bizTag;
    this.binderType = binderType;
    this.bindingCfg = bindingCfg;
    this.binders =
        binders != null ? Collections.unmodifiableList(binders) : Collections.emptyList();
    this.applicationContext = applicationContext;
  }

  /** 获取第一个驱动 (快捷方法，适用于单驱动场景) */
  public B getFirstBinder() {
    return binders.isEmpty() ? null : binders.get(0);
  }

  public String getBizTag() {
    return bizTag;
  }

  public String getBinderType() {
    return binderType;
  }

  public C getBindingCfg() {
    return bindingCfg;
  }

  public List<B> getBinders() {
    return binders;
  }

  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }
}
