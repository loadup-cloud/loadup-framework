package com.github.loadup.components.extension.spi;

import com.github.loadup.components.extension.api.IExtensionPoint;

/**
 * Extension Provider SPI Interface
 * 扩展点提供者 SPI 接口
 *
 * <p>通过实现此接口，可以动态加载和注册扩展点实现。
 * 实现类需要在 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
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

