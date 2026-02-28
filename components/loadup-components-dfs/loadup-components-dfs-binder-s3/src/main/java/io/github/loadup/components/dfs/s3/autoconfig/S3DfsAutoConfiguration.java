package io.github.loadup.components.dfs.s3.autoconfig;

/*-
 * #%L
 * Loadup Dfs Binder S3
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

import io.github.loadup.components.dfs.binding.DefaultDfsBinding;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.s3.binder.S3DfsBinder;
import io.github.loadup.components.dfs.s3.cfg.S3DfsBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(S3DfsBinder.class)
public class S3DfsAutoConfiguration {

    /**
     * S3 模块的自动注册逻辑 只有当 classpath 下存在 S3DfsBinding 类时（即引入了 binder-s3 依赖），这段逻辑才生效
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> s3Metadata() {
        return new BindingMetadata<>(
                "s3",
                DefaultDfsBinding.class,
                S3DfsBinder.class,
                DfsBindingCfg.class,
                S3DfsBinderCfg.class,
                ctx -> new DefaultDfsBinding());
    }
}
