package com.github.loadup.framework.api.core;

import com.github.loadup.framework.api.annotation.BindingClient;
import com.github.loadup.framework.api.manager.BindingManagerSupport;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

// @Component
public class BindingPostProcessor implements BeanPostProcessor, PriorityOrdered {

  private final ApplicationContext context;

  public BindingPostProcessor(ApplicationContext context) {
    this.context = context;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE; // 尽早执行注入
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    ReflectionUtils.doWithFields(
        bean.getClass(),
        field -> {
          BindingClient annotation = field.getAnnotation(BindingClient.class);
          if (annotation != null) {
            Object instance = resolveBinding(field.getType(), annotation.value());
            if (instance != null) {
              ReflectionUtils.makeAccessible(field);
              field.set(bean, instance);
            }
          }
        });
    return bean;
  }

  private Object resolveBinding(Class<?> targetType, String bizTag) {
    // 遍历容器中所有的 ManagerSupport
    Map<String, BindingManagerSupport> managers =
        context.getBeansOfType(BindingManagerSupport.class);
    for (BindingManagerSupport manager : managers.values()) {
      try {
        Object binding = manager.getBinding(bizTag);
        if (targetType.isAssignableFrom(binding.getClass())) {
          return binding;
        }
      } catch (Exception ignored) {
      }
    }
    return null;
  }
}
