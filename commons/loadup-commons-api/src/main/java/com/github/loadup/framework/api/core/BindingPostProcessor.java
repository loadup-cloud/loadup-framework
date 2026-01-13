package com.github.loadup.framework.api.core;

import com.github.loadup.framework.api.annotation.BindingClient;
import com.github.loadup.framework.api.manager.BindingManagerSupport;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

// @Component
public class BindingPostProcessor implements BeanPostProcessor, PriorityOrdered {

  private final ApplicationContext context;
  // 缓存：字段类型 -> 对应的 Manager 实例，避免频繁搜索容器
  private final Map<Class<?>, BindingManagerSupport<?, ?>> managerCache = new ConcurrentHashMap<>();

  public BindingPostProcessor(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public int getOrder() {
    // 设置最高优先级，确保在业务 Bean 初始化前完成注入
    return PriorityOrdered.HIGHEST_PRECEDENCE;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    Class<?> beanClass = bean.getClass();

    ReflectionUtils.doWithFields(
        beanClass,
        field -> {
          BindingClient annotation = field.getAnnotation(BindingClient.class);
          if (annotation != null) {
            injectBinding(bean, field, annotation);
          }
        });

    return bean;
  }

  private void injectBinding(Object bean, Field field, BindingClient annotation) {
    String bizTag = annotation.value();
    Class<?> fieldType = field.getType();

    // 1. 寻找能够处理该字段类型的 Manager
    BindingManagerSupport<?, ?> manager =
        managerCache.computeIfAbsent(fieldType, this::findManagerForType);

    if (manager == null) {
      throw new IllegalStateException(
          "未找到支持类型 [" + fieldType.getName() + "] 的 BindingManager，请检查 Starter 是否引入。");
    }

    // 2. 从 Manager 获取具体的 Binding 实例 (内部已处理泛型推断)
    Object binding = manager.getBinding(bizTag);

    // 3. 反射注入
    ReflectionUtils.makeAccessible(field);
    try {
      field.set(bean, binding);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("无法注入 Binding 到字段: " + field.getName(), e);
    }
  }

  /** 在 Spring 容器中动态寻找 Manager */
  @SuppressWarnings("rawtypes")
  private BindingManagerSupport<?, ?> findManagerForType(Class<?> targetType) {
    Map<String, BindingManagerSupport> allManagers =
        context.getBeansOfType(BindingManagerSupport.class);

    for (BindingManagerSupport manager : allManagers.values()) {
      // 通过我们在 Manager 中定义的 getBindingInterface() 来判断适配性
      // 比如 DfsBindingManager 返回 DfsBinding.class
      // 如果字段类型是 DfsBinding 或其子类，则匹配成功
      if (manager.getBindingInterface().isAssignableFrom(targetType)) {
        return manager;
      }
    }
    return null;
  }
}
