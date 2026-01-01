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

  // 场景坐标缓存，避免重复查找 - 使用 WeakHashMap 防止类加载器泄漏
  private final Map<ScenarioKey, List<ExtensionCoordinate>> scenarioCache =
      Collections.synchronizedMap(new WeakHashMap<>());

  // 是否已完成初始化扫描
  private volatile boolean initialized = false;

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

      // 3. 预热缓存 - 为所有已知的扩展点类型和场景组合构建缓存
      prewarmCache();

      initialized = true;

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
    // 如果已初始化，使用缓存
    if (initialized) {
      ScenarioKey key = new ScenarioKey(extensionPointClass, scenario);
      return scenarioCache.computeIfAbsent(key, k -> computeExtensionsByScenario(extensionPointClass, scenario));
    }
    // 未初始化时直接计算
    return computeExtensionsByScenario(extensionPointClass, scenario);
  }

  /** 计算匹配场景的扩展点 */
  private <T> List<ExtensionCoordinate> computeExtensionsByScenario(
      Class<T> extensionPointClass, BizScenario scenario) {
    List<ExtensionCoordinate> candidates = getExtensionCoordinates(extensionPointClass);
    return candidates.stream()
        .filter(c -> matchesScenario(c.extensionMetadata(), scenario))
        .collect(Collectors.toList());
  }

  /** 预热缓存 - 为常见场景构建缓存映射 */
  private void prewarmCache() {
    log.debug("Prewarming extension cache...");
    int cacheEntries = 0;
    
    // 遍历所有扩展点类型
    for (Map.Entry<Class<?>, List<ExtensionCoordinate>> entry : extensionRegister.entrySet()) {
      Class<?> extensionType = entry.getKey();
      List<ExtensionCoordinate> coordinates = entry.getValue();
      
      // 为每个扩展坐标创建场景并缓存
      for (ExtensionCoordinate coordinate : coordinates) {
        Extension metadata = coordinate.extensionMetadata();
        BizScenario scenario = BizScenario.valueOf(
            metadata.bizCode(), 
            metadata.useCase(), 
            metadata.scenario()
        );
        
        ScenarioKey key = new ScenarioKey(extensionType, scenario);
        scenarioCache.put(key, computeExtensionsByScenario(extensionType, scenario));
        cacheEntries++;
      }
    }
    
    log.debug("Extension cache prewarmed with {} entries", cacheEntries);
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

  /** 场景缓存键 - 用于缓存查找 */
  private static class ScenarioKey {
    private final Class<?> extensionType;
    private final String bizCode;
    private final String useCase;
    private final String scenario;

    public ScenarioKey(Class<?> extensionType, BizScenario bizScenario) {
      this.extensionType = extensionType;
      this.bizCode = bizScenario.getBizCode();
      this.useCase = bizScenario.getUseCase();
      this.scenario = bizScenario.getScenario();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ScenarioKey that = (ScenarioKey) o;
      return Objects.equals(extensionType, that.extensionType)
          && Objects.equals(bizCode, that.bizCode)
          && Objects.equals(useCase, that.useCase)
          && Objects.equals(scenario, that.scenario);
    }

    @Override
    public int hashCode() {
      return Objects.hash(extensionType, bizCode, useCase, scenario);
    }
  }
}
