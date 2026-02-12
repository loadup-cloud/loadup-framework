package io.github.loadup.framework.api.binding;

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
import io.github.loadup.framework.api.context.BindingContext;
import java.util.List;

public abstract class AbstractBinding<B extends Binder, S extends BaseBindingCfg> implements Binding<B, S> {
    protected S bindingCfg;
    protected List<B> binders;
    protected String bizTag;
    protected String binderType;
    private BindingContext<B, S> context; // 核心上下文

    /**
     * 获取首选驱动（通常 binders 列表中至少有一个）
     */
    protected B getBinder() {
        if (binders == null || binders.isEmpty()) {
            throw new IllegalStateException("No binders available for " + bizTag);
        }
        // 默认返回第一个驱动
        return binders.getFirst();
    }

    public S getBindingCfg() {
        return bindingCfg;
    }

    public void setBindingCfg(S bindingCfg) {
        this.bindingCfg = bindingCfg;
    }

    public List<B> getBinders() {
        return binders;
    }

    public void setBinders(List<B> binders) {
        this.binders = binders;
    }

    public String getName() {
        return bizTag;
    }

    public void setBizTag(String bizTag) {
        this.bizTag = bizTag;
    }

    public String getBinderType() {
        return binderType;
    }

    public void setBinderType(String binderType) {
        this.binderType = binderType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(BindingContext<B, S> context) {
        this.context = context;
        this.bizTag = context.getBizTag();
        this.binderType = context.getBinderType();
        // 这里的强制转型是安全的，因为 Manager 保证了传入的类型匹配
        this.bindingCfg = context.getBindingCfg();
        this.binders = context.getBinders();

        // 留给子类的钩子
        afterInit();
    }

    /**
     * 子类扩展点：如果 S3Binding 需要在拿到配置后初始化 AmazonS3 客户端，写在这里
     */
    protected void afterInit() {
        getBinder().binderInit();
    }

    public final void destroy() {
        afterDestroy();
    }

    protected void afterDestroy() {
        // 默认空实现
    }

    public AbstractBinding<B, S> setContext(BindingContext<B, S> context) {
        this.context = context;
        return this;
    }

    protected BindingContext<B, S> getContext() {
        return this.context;
    }

    @Override
    public String getBizTag() {
        return bizTag;
    }
}
