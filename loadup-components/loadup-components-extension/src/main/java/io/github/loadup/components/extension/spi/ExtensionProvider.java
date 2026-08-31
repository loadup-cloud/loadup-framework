package io.github.loadup.components.extension.spi;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.github.loadup.components.extension.api.IExtensionPoint;

/**
 * Extension Provider SPI Interface 扩展点提供者 SPI 接口
 *
 * <p>通过实现此接口，可以动态加载和注册扩展点实现。 实现类需要在
 * META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 * 或通过 @Configuration 注解来注册。
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface ExtensionProvider {

    /**
     * 获取扩展点类型
     *
     * @return 扩展点接口类
     */
    Class<? extends IExtensionPoint> getExtensionType();

    /**
     * 获取业务代码
     *
     * @return 业务代码标识
     */
    String getBizCode();

    /**
     * 获取用例
     *
     * @return 用例标识，默认 "default"
     */
    default String getUseCase() {
        return "default";
    }

    /**
     * 获取场景
     *
     * @return 场景标识，默认 "default"
     */
    default String getScenario() {
        return "default";
    }

    /**
     * 获取优先级
     *
     * @return 优先级，数值越小优先级越高，默认为 0
     */
    default int getPriority() {
        return 0;
    }

    /**
     * 创建扩展点实例
     *
     * @return 扩展点实现实例
     */
    IExtensionPoint createExtension();
}
