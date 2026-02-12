package io.github.loadup.framework.api.cfg;

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

import io.github.loadup.framework.api.binder.BinderConfig;

import java.util.Objects;

public abstract class BaseBinderCfg implements BinderConfig {
    protected String name;
    protected String binder;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBinder() {
        return binder;
    }

    public void setBinder(String binder) {
        this.binder = binder;
    }

    // 强制子类重写这个方法，否则编译不通过
    @Override
    public abstract Object getIdentity();

    // 统一定义 hashCode 逻辑，防止子类随意发挥
    @Override
    public final int hashCode() {
        Object id = getIdentity();
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BaseBinderCfg)) return false;
        return Objects.equals(this.getIdentity(), ((BaseBinderCfg) obj).getIdentity());
    }
}
