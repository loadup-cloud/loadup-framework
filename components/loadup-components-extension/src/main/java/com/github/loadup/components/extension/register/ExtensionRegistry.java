package com.github.loadup.components.extension.register;

/*-
 * #%L
 * loadup-components-extension
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

import com.github.loadup.components.extension.annotation.Extension;
import com.github.loadup.components.extension.api.IExtensionPoint;
import com.github.loadup.components.extension.core.BizScenario;
import com.github.loadup.components.extension.spi.ExtensionProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/** 扩展点注册中心 支持基于注解的扩展点注册和 SPI 机制的扩展点加载 */
@Slf4j
public class ExtensionRegistry implements ApplicationListener<ContextRefreshedEvent> {

  // 内部存储结构，Key是扩展点接口类，Value是该接口的所有实现
  private final Map<Class<?>, List<ExtensionCoordinate>> extensionRegister =
      new ConcurrentHashMap<>();

  // 按 bizCode 索引的扩展点快速查找
  private final Map<String, List<ExtensionCoordinate>> bizCodeIndex = new ConcurrentHashMap<>();

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    ApplicationContext applicationContext = event.getApplicationContext();
    // 避免在父子容器环境中重复注册
    if (applicationContext.getParent() == null) {
      log.info("Starting to register all extensions...");

      // 1. 注册基于注解的扩展点
      Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Extension.class);
      beans.values().forEach(this::registerExtension);

      // 2. 加载 SPI 扩展点提供者
      Map<String, ExtensionProvider> providers =
          applicationContext.getBeansOfType(ExtensionProvider.class);
      providers.values().forEach(this::registerSpiExtension);

      log.info(
          "Extension registration completed. Total {} extension point types loaded with {} implementations.",
          extensionRegister.size(),
          extensionRegister.values().stream().mapToInt(List::size).sum());
    }
  }

  private void registerExtension(Object bean) {
    Class<?> beanClass = bean.getClass();
    Extension extensionAnn = beanClass.getAnnotation(Extension.class);
    if (extensionAnn == null) {
      return;
    }

    // 找到该Bean实现的所有IExtensionPoint接口
    Class<?>[] interfaces = beanClass.getInterfaces();
    for (Class<?> intf : interfaces) {
      if (IExtensionPoint.class.isAssignableFrom(intf)) {
        ExtensionCoordinate coordinate = new ExtensionCoordinate(bean, extensionAnn);
        registerCoordinate(intf, coordinate);
        log.debug(
            "Registered extension [{} | bizCode:{}, useCase:{}, scenario:{}] for interface [{}].",
            bean.getClass().getSimpleName(),
            extensionAnn.bizCode(),
            extensionAnn.useCase(),
            extensionAnn.scenario(),
            intf.getSimpleName());
      }
    }
  }

  private void registerSpiExtension(ExtensionProvider provider) {
    Class<? extends IExtensionPoint> extensionType = provider.getExtensionType();
    IExtensionPoint instance = provider.createExtension();

    // 创建一个虚拟的 Extension 注解
    Extension virtualExtension =
        new Extension() {
          @Override
          public Class<? extends java.lang.annotation.Annotation> annotationType() {
            return Extension.class;
          }

          @Override
          public String bizCode() {
            return provider.getBizCode();
          }

          @Override
          public String useCase() {
            return provider.getUseCase();
          }

          @Override
          public String scenario() {
            return provider.getScenario();
          }

          @Override
          public int priority() {
            return provider.getPriority();
          }
        };

    ExtensionCoordinate coordinate = new ExtensionCoordinate(instance, virtualExtension);
    registerCoordinate(extensionType, coordinate);

    log.debug(
        "Registered SPI extension [{} | bizCode:{}, useCase:{}, scenario:{}] for interface [{}].",
        instance.getClass().getSimpleName(),
        provider.getBizCode(),
        provider.getUseCase(),
        provider.getScenario(),
        extensionType.getSimpleName());
  }

  private void registerCoordinate(Class<?> extensionType, ExtensionCoordinate coordinate) {
    // 注册到主索引
    List<ExtensionCoordinate> coordinates =
        extensionRegister.computeIfAbsent(extensionType, k -> new ArrayList<>());
    coordinates.add(coordinate);

    // 注册到 bizCode 索引
    String bizCode = coordinate.extensionMetadata().bizCode();
    List<ExtensionCoordinate> bizCodeList =
        bizCodeIndex.computeIfAbsent(bizCode, k -> new ArrayList<>());
    bizCodeList.add(coordinate);
  }

  /** 根据扩展点接口类获取其所有实现 */
  public <T> List<ExtensionCoordinate> getExtensionCoordinates(Class<T> extensionPointClass) {
    return extensionRegister.getOrDefault(extensionPointClass, Collections.emptyList());
  }

  /** 根据 bizCode 获取所有匹配的扩展点 */
  public List<ExtensionCoordinate> getExtensionsByBizCode(String bizCode) {
    return bizCodeIndex.getOrDefault(bizCode, Collections.emptyList());
  }

  /** 根据 BizScenario 获取匹配的扩展点 */
  public <T> List<ExtensionCoordinate> getExtensionsByScenario(
      Class<T> extensionPointClass, BizScenario scenario) {
    List<ExtensionCoordinate> candidates = getExtensionCoordinates(extensionPointClass);
    return candidates.stream()
        .filter(c -> matchesScenario(c.extensionMetadata(), scenario))
        .collect(Collectors.toList());
  }

  /** 获取所有注册的扩展点类型 */
  public Set<Class<?>> getAllExtensionTypes() {
    return extensionRegister.keySet();
  }

  /** 获取所有注册的 bizCode */
  public Set<String> getAllBizCodes() {
    return bizCodeIndex.keySet();
  }

  private boolean matchesScenario(Extension extension, BizScenario scenario) {
    return extension.bizCode().equals(scenario.getBizCode())
        && extension.useCase().equals(scenario.getUseCase())
        && extension.scenario().equals(scenario.getScenario());
  }
}
